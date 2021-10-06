package com.wadiyatalkinabeet.gambit.cv.cornerdetection.v2

import com.wadiyatalkinabeet.gambit.cv.*
import com.wadiyatalkinabeet.gambit.cv.Point
import com.wadiyatalkinabeet.gambit.math.algorithms.*
import com.wadiyatalkinabeet.gambit.math.datastructures.Line
import com.wadiyatalkinabeet.gambit.math.datastructures.inverse
import com.wadiyatalkinabeet.gambit.math.datastructures.toMat
import com.wadiyatalkinabeet.gambit.math.datastructures.toMatrix
import com.wadiyatalkinabeet.gambit.math.statistics.clustering.AverageAgglomerative
import com.wadiyatalkinabeet.gambit.math.statistics.clustering.ClusteringException
import com.wadiyatalkinabeet.gambit.math.statistics.clustering.DBScan
import com.wadiyatalkinabeet.gambit.math.statistics.clustering.FCluster
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.*


fun resize(
    src: Mat,
    horizontalSize: Double = 1200.0
): Pair<Mat, Double> {
    val w: Double = src.width().toDouble()
    val h: Double = src.height().toDouble()
    val scale: Double = horizontalSize / w
    val dst = Mat()
    resize(src, dst, Size(horizontalSize, h * scale))
    return Pair(dst, scale)
}

fun detectLines(
    src: Mat,
    eliminateDiagonals: Boolean = false,
): List<Line> {

    val tmpMat = Mat()
    src.convertTo(src, CV_8UC1)
    canny(src, tmpMat, 90.0, 400.0, 3)
    houghLines(tmpMat, tmpMat, 1.0, PI/360, 100)

    var allLines = Line.fromHoughLines(tmpMat)
    // Looks like this would break diagonal views if enabled
    if (eliminateDiagonals) {
        fun diagonalFilter(line: Line) = (abs(line.theta) < 0.524 || abs(line.theta - (PI / 2)) < 0.524 )
            allLines = allLines.filter(::diagonalFilter)
    }
    return allLines
}

fun fCluster(lines: List<Line>, maxAngle: Double = PI/180): Pair<List<Line>, List<Line>>? {
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

fun clusterLines(lines: List<Line>): Pair<List<Line>, List<Line>> {
    val (lineGroup0, lineGroup1) =
        AverageAgglomerative(lines, numClusters = 2).runClustering()
            .map { cluster -> cluster.value.map { lines[it] } }
            .let{ Pair(it[0], it[1]) }

    val averageAngleToYAxisGroup0 = lineGroup0.map { it.angleTo(Line(0.0f,0.0f)) }.average()
    val averageAngleToYAxisGroup1 = lineGroup1.map { it.angleTo(Line(0.0f,0.0f)) }.average()

    return if (averageAngleToYAxisGroup0 > averageAngleToYAxisGroup1) Pair(lineGroup0, lineGroup1)
    else Pair(lineGroup1, lineGroup0)
}

fun makeBorderMat(size: Size, type: Int, borderThickness: Int = 3): Mat{
    val bordersMat = Mat.zeros(size, type)
    for (i in 3 until bordersMat.rows()-borderThickness){
        for (j in 3 until bordersMat.cols()-borderThickness){
            bordersMat.put(i, j, 255.0)
        }
    }
    return bordersMat
}

fun findCorners(src: Mat): List<com.wadiyatalkinabeet.gambit.math.datastructures.Point?>? {
    //Resize
    val (grayscaleMat, scale) = resize(src)

    // Detect all lines
    val detectedLines = detectLines(grayscaleMat, eliminateDiagonals = false)
//        .also { if (it.size > 800) return null }

    // Cluster lines into vertical and horizontal
    var (horizontalLines, verticalLines) = try {
        clusterLines(detectedLines)
    } catch (e: ClusteringException) {
        null
    } ?: run { return null }

    // Eliminate similar lines within groups
    horizontalLines = eliminateSimilarLines(horizontalLines, verticalLines)
    verticalLines = eliminateSimilarLines(verticalLines, horizontalLines)
    if (horizontalLines.size <= 2 || verticalLines.size <= 2){
        return null
    }

    // Find intersections between remaining points
    val allIntersectionPoints = findIntersectionPoints(horizontalLines, verticalLines)
    if (allIntersectionPoints.size * allIntersectionPoints[0].size < 4){
        return null
    }

    val ransacConfiguration = try {
        runRANSAC(allIntersectionPoints)
    } catch (e: RANSACException) {
        null
    } ?: run { return null }

    if (ransacConfiguration.intersectionPoints.size*ransacConfiguration.intersectionPoints[0].size < 4){
        return null
    }

    val transformationMat = findHomography(
            MatOfPoint2f(*ransacConfiguration.intersectionPoints.flatten().filterNotNull().toTypedArray()),
            MatOfPoint2f(*ransacConfiguration.quantizedPoints.flatten().filterNotNull().toTypedArray())
        )

    val warpedGrayscaleMat = Mat()
    warpPerspective(
        grayscaleMat, warpedGrayscaleMat,
        transformationMat, ransacConfiguration.warpedImageSize
    )

    val warpedBordersMat = Mat()
    warpPerspective(
        makeBorderMat(grayscaleMat.size(), CV_8UC1, 3),
        warpedBordersMat, transformationMat, ransacConfiguration.warpedImageSize
    )

    val (xMin, xMax) = try {
        computeVerticalBorders(
            warpedGrayscaleMat, warpedBordersMat, ransacConfiguration.scale,
            ransacConfiguration.xMin, ransacConfiguration.xMax
        )
    } catch (e: ImageProcessingException) { return null }

    val scaledXMin = ransacConfiguration.scale.first*xMin
    val scaledXMax = ransacConfiguration.scale.first*xMax

    for (i in 0 until warpedBordersMat.rows()){
        for (j in 0 until warpedBordersMat.cols()){
            if (i < scaledXMin || i > scaledXMax){
                warpedBordersMat.put(i, j, 0.0)
            }
        }
    }

    val (yMin, yMax) = try {
        computeHorizontalBorders(
            warpedGrayscaleMat, warpedBordersMat, ransacConfiguration.scale,
            ransacConfiguration.yMin, ransacConfiguration.yMax
        )
    } catch (e: ImageProcessingException) { return null }

    return warpPoints(
        arrayOf(
            Point((ransacConfiguration.scale.first*xMin).toDouble(), (ransacConfiguration.scale.second*yMin).toDouble()),
            Point((ransacConfiguration.scale.first*xMax).toDouble(), (ransacConfiguration.scale.second*yMin).toDouble()),
            Point((ransacConfiguration.scale.first*xMax).toDouble(), (ransacConfiguration.scale.second*yMax).toDouble()),
            Point((ransacConfiguration.scale.first*xMin).toDouble(), (ransacConfiguration.scale.second*yMax).toDouble())
        ), transformationMat.toMatrix().inverse().toMat()
    ).map { it
        ?.let{
            com.wadiyatalkinabeet.gambit.math.datastructures.Point(
                (it.x/scale).toFloat(), (it.y/scale).toFloat()
            )
        } }
}

fun computeVerticalBorders(
    warpedGrayscaleMat: Mat, warpedBorderMat: Mat,
    scale: Pair<Int, Int>, xMin: Int, xMax: Int
): Pair<Int, Int>{
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
        val xScaled = x * scale.first

        val colCounts = mutableMapOf<Int, Int>()
        for (i in 0 until resultMat.rows()){
            for (j in (xScaled-2 until xScaled+3)){
                colCounts[j] = (colCounts[j] ?: 0)+1
            }
        }
        return colCounts.filterKeys { 0 < it && it < resultMat.cols() }.maxByOrNull { it.value }?.let {
            resultMat.col(it.key)
        }
    }

    var xMaxScaled = xMax
    var xMinScaled = xMin

    while (xMaxScaled - xMinScaled < 8){
        val top = getNonMaxSuppressed(xMaxScaled + 1) ?: run { throw ImageProcessingException("Failed to detect vertical borders")}
        val bottom = getNonMaxSuppressed(xMinScaled - 1) ?: run { throw ImageProcessingException("Failed to detect vertical borders")}

        if (top.toMatrix().map { it.sum() }.sum() > bottom.toMatrix().map { it.sum() }.sum()){
            xMaxScaled += 1
        } else{
            xMinScaled -= 1
        }
    }

    return Pair(xMinScaled, xMaxScaled)
}

fun computeHorizontalBorders(
    warpedGrayscaleMat: Mat, warpedBorderMat: Mat,
    scale: Pair<Int, Int>, yMin: Int, yMax: Int
): Pair<Int, Int>{
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
        val yScaled = y * scale.second

        val rowCounts = mutableMapOf<Int, Int>()
        for (i in (yScaled-2 until yScaled+3) ){
            for (j in 0 until resultMat.cols()){
                rowCounts[i] = (rowCounts[i] ?: 0) + 1
            }
        }
        return rowCounts.filterKeys { 0 < it && it < resultMat.rows() }.maxByOrNull { it.value }?.let {
            resultMat.row(it.key)
        }
    }

    var yMaxScaled = yMax
    var yMinScaled = yMin

    while (yMaxScaled - yMinScaled < 8){
        val top = getNonMaxSuppressed(yMaxScaled + 1) ?: run { throw ImageProcessingException("Failed to detect horizontal border") }
        val bottom = getNonMaxSuppressed(yMinScaled - 1) ?: run { throw ImageProcessingException("Failed to detect horizontal border") }

        if (top.toMatrix().map { it.sum() }.sum() > bottom.toMatrix().map { it.sum() }.sum()){
            yMaxScaled += 1
        } else{
            yMinScaled -= 1
        }
    }

    return Pair(yMinScaled, yMaxScaled)
}

fun findIntersectionPoints(
    lines: List<Line>, perpendicularLines: List<Line>
): Array<Array<Point?>> {

    val (rho1, rho2) = meshGrid(lines.map { it.rho }.toFloatArray(), perpendicularLines.map { it.rho }.toFloatArray(), MeshgridIndex.IJ)
    val (theta1, theta2) = meshGrid(lines.map { it.theta }.toFloatArray(), perpendicularLines.map { it.theta }.toFloatArray(), MeshgridIndex.IJ)

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

fun intersection(
    rho1: Float, theta1: Float, rho2: Float, theta2: Float
): Point? {
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

class ImageProcessingException(message: String): Exception(message)