package com.wadiyatalkinabeet.gambit.cv.cornerdetection.v2

import com.wadiyatalkinabeet.gambit.cv.*
import com.wadiyatalkinabeet.gambit.cv.Point
import com.wadiyatalkinabeet.gambit.math.algorithms.*
import com.wadiyatalkinabeet.gambit.math.datastructures.Line
import com.wadiyatalkinabeet.gambit.math.datastructures.inverse
import com.wadiyatalkinabeet.gambit.math.datastructures.toMat
import com.wadiyatalkinabeet.gambit.math.datastructures.toMatrix
import com.wadiyatalkinabeet.gambit.math.statistics.clustering.AverageAgglomerative
import com.wadiyatalkinabeet.gambit.math.statistics.clustering.DBScan
import com.wadiyatalkinabeet.gambit.math.statistics.clustering.FCluster
import java.lang.IndexOutOfBoundsException
import kotlin.math.*

fun resize(
    src: Mat,
    dst: Mat,
    numPixels: Float = 500f.pow(2f),
): Double {
    val w: Double = src.width().toDouble()
    val h: Double = src.height().toDouble()
    val scale: Double = sqrt(numPixels / (w * h))
    resize(src, dst, Size(w * scale, h * scale))
    return scale
}

fun detectEdges(src: Mat, edges: Mat){
}

fun detectLines(
    edges: Mat,
    eliminateDiagonals: Boolean = false,
): List<Line> {
    val tmpMat = Mat()
    houghLines(edges, tmpMat, 1.0, PI/360, 100)
    var allLines = Line.fromHoughLines(tmpMat)

    // Looks like this would break diagonal views if enabled
    if (eliminateDiagonals) {
        fun diagonalFilter(line: Line) = (abs(line.theta) < 0.524 || abs(line.theta - (PI / 2)) < 0.524 )
            allLines = allLines.filter(::diagonalFilter)
    }

    return allLines
}

//TODO This needs to use HCluster, not FCluster
fun cluster(lines: List<Line>, maxAngle: Double = PI/180): Pair<List<Line>, List<Line>>? {
    val a = FCluster.apply(lines.size) { index1, index2 ->
        lines[index1].angleTo(lines[index2]) <= maxAngle
    }
    val allClusters =  a.map { cluster ->
        cluster?.map {
            it?.let { lines[it] } ?: return null
        } ?: return null
    }
    if (allClusters.size < 2)
        return null

    return allClusters.sortedBy{-it.size}.take(2).let{Pair(it[0], it[1])}
}

fun eliminateSimilarLines(lines: List<Line>, perpendicularLines: List<Line>): List<Line> {
    val perpendicular = perpendicularLines
        .fold(
            initial = Line(0.0f, 0.0f),
            operation = { mean: Line, line: Line ->
                Line(mean.rho + line.rho, mean.theta + line.theta)
            })
        .let{
            Line(it.rho / perpendicularLines.size, it.theta / perpendicularLines.size)
        }

    val nullIndices: MutableList<Int> = mutableListOf()
    val intersections = lines.mapIndexed { i, line ->
        line.intersection(perpendicular) ?: run {
            nullIndices.add(i)
        }.let{ null }
    }.filterNotNull()

    // Filter out lines with no intersection
    val goodLines = if (nullIndices.isNotEmpty()) {
        lines.filterIndexed { i, _ -> !nullIndices.contains(i) }
    } else lines

    // Now run DB scan and throw away all lines except those that have the first point in each cluster
    return DBScan(intersections).map {
        goodLines[it.first()]
    }
}

fun findCorners(src: Mat): Pair<List<Line>, List<Line>>? {
    val grayscaleMat = Mat()
    val scale = resize(src, grayscaleMat)
    cvtColor(grayscaleMat, grayscaleMat, COLOR_BGR2GRAY)

    val edgesMat = Mat()
    grayscaleMat.convertTo(grayscaleMat, CV_8UC1)
    canny(grayscaleMat, edgesMat, 90.0, 400.0, 3)
    val allLines = detectLines(edgesMat)

    // Originally 400
    if (allLines.size > 400){
        return null
    }

    val agglomerative = AverageAgglomerative(allLines, numClusters = 2)
    agglomerative.run()

    val (lineGroup0, lineGroup1) = agglomerative.clusters
            .map { cluster -> cluster.value.map { allLines[it] } }
            .also { if (it.size != 2) return null }
            .let{ Pair(it[0], it[1]) }


    val averageAngleToYAxisGroup0 = lineGroup0.map { it.angleTo(Line(0.0f,0.0f)) }.sum() / lineGroup0.size
    val averageAngleToYAxisGroup1 = lineGroup1.map { it.angleTo(Line(0.0f,0.0f)) }.sum() / lineGroup1.size

    var (horizontalLines, verticalLines) = if (averageAngleToYAxisGroup0 > averageAngleToYAxisGroup1)
       Pair(lineGroup0, lineGroup1) else
        Pair(lineGroup1, lineGroup0)

    horizontalLines = eliminateSimilarLines(horizontalLines, verticalLines)
    verticalLines = eliminateSimilarLines(verticalLines, horizontalLines)

    if (horizontalLines.size <=2 || verticalLines.size <= 2){
        return null
    }

    val allIntersectionPoints = findIntersectionPoints(horizontalLines, verticalLines).map { it.toMutableList() }.toMutableList()

    var bestNumInliers = 0
    var bestRansacConfig: RANSACConfiguration? = null
    var epoch = 0
    val nIterations = 200
    while (bestNumInliers < 30 || epoch < nIterations) {
        epoch++
        val rowIndices = horizontalLines.indices.shuffled().take(2)
        val colIndices = verticalLines.indices.shuffled().take(2)

        val transformationMatrix = computeHomography(
            intersectionPoints = allIntersectionPoints,
            rowIndex1 = rowIndices[0],
            rowIndex2 = rowIndices[1],
            colIndex1 = colIndices[0],
            colIndex2 = colIndices[1]
        )

        var warpedPoints =
            warpPoints(allIntersectionPoints, transformationMatrix).map { it.toMutableList() }
                .toMutableList()

        val (rowsAndColsToKeep, scales) = try {
            discardOutliers(warpedPoints)
        } catch (e: RANSACException) {
            continue
        }

        warpedPoints = warpedPoints
            .filterIndexed { i, _ -> rowsAndColsToKeep.first.contains(i) }
            .map {
                it.filterIndexed { j, _ -> rowsAndColsToKeep.second.contains(j) }
                    .toMutableList()
            }.toMutableList()

        val intersectionPoints = allIntersectionPoints
            .filterIndexed { i, _ -> rowsAndColsToKeep.first.contains(i) }
            .map {
                it.filterIndexed { j, _ -> rowsAndColsToKeep.second.contains(j) }
                    .toMutableList()
            }.toMutableList()

        val numInliers = try { warpedPoints.size * warpedPoints[0].size } catch (e: IndexOutOfBoundsException) { continue }
        if (numInliers > bestNumInliers){
            for (i in warpedPoints.indices){
                for (j in warpedPoints[i].indices){
                    warpedPoints[i][j]?.let {
                        warpedPoints[i][j] = Point(scales.first * it.x, scales.second * it.y)
                    }
                }
            }

            val ransacConfig = quantizePoints(warpedScaledPoints = warpedPoints, intersectionPoints)

            val numInliers = try{ ransacConfig.scaledQuantizedPoints.first.size * ransacConfig.scaledQuantizedPoints.first[0].size } catch (e: IndexOutOfBoundsException){ continue }

            if (numInliers > bestNumInliers){
                bestNumInliers = numInliers
                bestRansacConfig = ransacConfig
            }
        }

        if (epoch > 1000){
            break
        }

    }
    bestRansacConfig?.let { bestConfig ->
        val transformationMat = findHomography(
            MatOfPoint2f(*bestConfig.intersectionPoints.flatten().filterNotNull().toTypedArray()),
            MatOfPoint2f(*bestConfig.scaledQuantizedPoints.first.indices
                .flatMap { i -> bestConfig.scaledQuantizedPoints.first[0].indices.map{ j -> i to j}
                }.map { (i, j) ->
                    Point(
                        bestConfig.scaledQuantizedPoints.first[i][j].toDouble(),
                        bestConfig.scaledQuantizedPoints.second[i][j].toDouble())
                }.toTypedArray()
            )
        )

        val transformationMatrix = transformationMat.toMatrix()
        val inverseTransformationMatrix = transformationMatrix.inverse()

        val warpedGrayscaleMat = Mat()
        warpPerspective(grayscaleMat, warpedGrayscaleMat, transformationMat, bestConfig.warpedImageSize)

        val borderMat = Mat.zeros(grayscaleMat.size(), CV_8UC1)
        for (i in 3 until borderMat.rows()-3){
            for (j in 3 until borderMat.cols()-3){
                borderMat.put(i, j, 255.0)
                //intArrayOf(1) instead?
            }
        }
       val warpedBordersMat = Mat()
        warpPerspective(borderMat, warpedBordersMat, transformationMat, bestConfig.warpedImageSize)

        val (xMin, xMax) = computeVerticalBorders(
            warpedGrayscaleMat, warpedBordersMat, bestConfig.scale,
            bestConfig.xMin, bestConfig.xMax
        )

        val scaledXMin = bestConfig.scale.first*xMin
        val scaledXMax = bestConfig.scale.first*xMax

        for (i in 0 until warpedBordersMat.rows()){
            for (j in 0 until warpedBordersMat.cols()){
                //Is this i or j
                if (i < scaledXMin || i > scaledXMax){
                    warpedBordersMat.put(i, j, 0.0)
                }
            }
        }

        val (yMin, yMax) = computeHorizontalBorders(
            warpedGrayscaleMat, warpedBordersMat, bestConfig.scale,
            bestConfig.yMin, bestConfig.yMax
        )

        var corners = warpPoints(
            listOf(
                listOf(
                Point((bestConfig.scale.first*xMin).toDouble(),
                    (bestConfig.scale.second*yMin).toDouble()
                ),
                Point((bestConfig.scale.first*xMax).toDouble(),
                    (bestConfig.scale.second*yMin).toDouble()
                )),
                listOf(
                Point((bestConfig.scale.first*xMax).toDouble(),
                    (bestConfig.scale.second*yMax).toDouble()
                ),
                Point((bestConfig.scale.first*xMin).toDouble(),
                    (bestConfig.scale.second*yMax).toDouble()
                )
                )
            ), inverseTransformationMatrix.toMat()
        ).flatten().map { it?.let { Point(it.x/scale, it.y/scale)} }
    }

    return Pair(
        horizontalLines,
        verticalLines
    )
}

fun computeVerticalBorders(warpedGrayscaleMat: Mat, warpedBorderMat: Mat, scale: Pair<Int, Int>, xMin: Int, xMax: Int): Pair<Int, Int>{
    val resultMat = Mat()
    sobel(warpedGrayscaleMat, resultMat, CV_64F, 1, 0, 3)

    for (i in 0 until resultMat.rows()){
        for (j in 0 until resultMat.cols()){
            if (warpedBorderMat[i, j][0] != 255.0){
                resultMat.put(i, j, 0.0)
            }
        }

    }
    resultMat.convertTo(resultMat, CV_8UC1)
    canny(resultMat, resultMat, 120.0, 300.0, 3)

    for (i in 0 until resultMat.rows()){
        for (j in 0 until resultMat.cols()){
            if (warpedBorderMat[i, j][0] != 255.0){
                resultMat.put(i, j, 0.0)
            }
        }

    }

    fun getNonMaxSuppressed(x: Int): Mat? {
        var xScaled = x * scale.first

        val colCounts = mutableMapOf<Int, Int>()
        for (i in 0 until resultMat.rows()){
            for (j in (xScaled-2 until xScaled+3)){
                colCounts[j] = (colCounts[0] ?: 0)+1
            }
        }
        return colCounts.maxByOrNull { it.value }?.let {
            resultMat.col(it.key)
        }
    }

    var xMaxScaled = xMax
    var xMinScaled = xMin

    while (xMaxScaled - xMinScaled < 8){
        val top = getNonMaxSuppressed(xMaxScaled + 1) ?: run { throw BorderDetectionException("Could not find borders")}
        val bottom = getNonMaxSuppressed(xMinScaled - 1) ?: run { throw BorderDetectionException("Could not find borders")}

        if (top.toMatrix().map { it.sum() }.sum() > bottom.toMatrix().map { it.sum() }.sum()){
            xMaxScaled += 1
        } else{
            xMinScaled -= 1
        }
    }

    return Pair(xMinScaled, xMaxScaled)
}

fun computeHorizontalBorders(warpedGrayscaleMat: Mat, warpedBorderMat: Mat, scale: Pair<Int, Int>, yMin: Int, yMax: Int): Pair<Int, Int>{
    val resultMat = Mat()
    sobel(warpedGrayscaleMat, resultMat, CV_64F, 0, 1, 3)

    for (i in 0 until resultMat.rows()){
        for (j in 0 until resultMat.cols()){
            if (warpedBorderMat[i, j][0] != 255.0){
                resultMat.put(i, j, 0.0)
            }
        }

    }
    resultMat.convertTo(resultMat, CV_8UC1)
    canny(resultMat, resultMat, 120.0, 300.0, 3)

    for (i in 0 until resultMat.rows()){
        for (j in 0 until resultMat.cols()){
            if (warpedBorderMat[i, j][0] != 255.0){
                resultMat.put(i, j, 0.0)
            }
        }

    }

    fun getNonMaxSuppressed(y: Int): Mat? {
        var yScaled = y * scale.second

        val rowCounts = mutableMapOf<Int, Int>()
        for (i in (yScaled-2 until yScaled+3) ){
            for (j in 0 until resultMat.cols()){
                rowCounts[i] = (rowCounts[0] ?: 0) + 1
            }
        }
        return rowCounts.maxByOrNull { it.value }?.let {
            resultMat.row(it.key)
        }
    }

    var yMaxScaled = yMax
    var yMinScaled = yMin

    while (yMaxScaled - yMinScaled < 8){
        val top = getNonMaxSuppressed(yMaxScaled + 1) ?: run { throw BorderDetectionException("Could not find borders")}
        val bottom = getNonMaxSuppressed(yMinScaled - 1) ?: run { throw BorderDetectionException("Could not find borders")}

        if (top.toMatrix().map { it.sum() }.sum() > bottom.toMatrix().map { it.sum() }.sum()){
            yMaxScaled += 1
        } else{
            yMinScaled -= 1
        }
    }

    return Pair(yMinScaled, yMaxScaled)
}

class BorderDetectionException(message: String): Exception(message)

fun findIntersectionPoints(horizontalLines: List<Line>, verticalLines: List<Line>): Array<Array<Point?>> {

    val (rho1, rho2) = meshGrid(horizontalLines.map { it.rho }.toFloatArray(), verticalLines.map { it.rho }.toFloatArray(), MeshgridIndex.IJ)
    val (theta1, theta2) = meshGrid(horizontalLines.map { it.theta }.toFloatArray(), verticalLines.map { it.theta }.toFloatArray(), MeshgridIndex.IJ)

    val intersectionPoints = Array(rho1.size) {
        Array<Point?>(rho1[0].size) { _ -> null }
    }
    for (i in rho1.indices){
        for (j in rho1[i].indices){
            intersection(rho1 = rho1[i][j], theta1 = theta1[i][j], rho2 = rho2[i][j], theta2 = theta2[i][j])?.let {
                intersectionPoints[i][j] = it
            }
        }

    }
    return intersectionPoints
}


fun intersection(rho1: Float, theta1: Float, rho2: Float, theta2: Float): Point? {
    val cos0 = cos(theta1)
    val cos1 = cos(theta2)
    val sin0 = sin(theta1)
    val sin1 = sin(theta2)
    return try {
        Point(
            (sin0 * rho2 - sin1 * rho1) / (cos1 * sin0 - cos0 * sin1).toDouble(),
            (cos0 * rho2 - cos1 * rho1) / (sin1 * cos0 - sin0 * cos1).toDouble()
        )
    } catch (_: ArithmeticException) {
        null
    }
}
