package com.wadiyatalkinabeet.gambit.math.algorithms

import com.wadiyatalkinabeet.gambit.domain.cv.*
import java.lang.IndexOutOfBoundsException
import java.lang.NullPointerException
import kotlin.math.abs
import kotlin.math.roundToInt


private fun warpPoint(
    x: Double,
    y: Double,
    z: Double,
    transformationMatrix: Mat
): Triple<Double, Double, Double> {

    val warpedX = (x * transformationMatrix.get(0,0)[0]) + (y * transformationMatrix.get(0,1)[0]) + (z * transformationMatrix.get(0,2)[0])
    val warpedY = (x * transformationMatrix.get(1,0)[0]) + (y * transformationMatrix.get(1,1)[0]) + (z * transformationMatrix.get(1,2)[0])
    val warpedZ = (x * transformationMatrix.get(2,0)[0]) + (y * transformationMatrix.get(2,1)[0]) + (z * transformationMatrix.get(2,2)[0])

    return Triple(warpedX, warpedY, warpedZ)
}

fun warpPoint(cornerPoint: Point, transformationMat: Mat): Point{
    val (warpedX, warpedY, _) = warpPoint(cornerPoint.x, cornerPoint.y, 1.0, transformationMat)
    return Point(warpedX, warpedY)
}

fun warpPoints(intersectionPoints: Array<Array<Point?>>, transformationMat: Mat): Array<Array<Point?>>{
    val warpedPoints = Array(intersectionPoints.size) {
        Array<Point?>(
            intersectionPoints[0].size
        ) {null}
    }
    for (i in intersectionPoints.indices){
        for (j in intersectionPoints[i].indices){
            intersectionPoints[i][j]?.let {
                val (warpedX, warpedY, _) = warpPoint(it.x, it.y, 1.0, transformationMat)
                warpedPoints[i][j] = Point(warpedX, warpedY)
            }
        }
    }
    return warpedPoints
}



private fun discardOutliers(
    warpedPoints: Array<Array<Point?>>,
    ascendingScales: IntArray = intArrayOf(1,2,3,4,5,6,7,8)
): Pair<Pair<Set<Int>, Set<Int>>, Pair<Int, Int>> {

    val scaleInlierCountsX = Array(ascendingScales.size){0}
    val scaleInlierCountsY = Array(ascendingScales.size){0}
    val inlierMaskX = Array(ascendingScales.size) {Array(warpedPoints.size){ Array(warpedPoints[0].size){false} }}
    val inlierMaskY = Array(ascendingScales.size) {Array(warpedPoints.size){ Array(warpedPoints[0].size){false} }}

    for (i in warpedPoints.indices){
        for (j in warpedPoints[i].indices){
            for (k in ascendingScales.indices){
                warpedPoints[i][j]?.let {
                    val scaledX = it.x * ascendingScales[k]
                    val scaledY = it.y * ascendingScales[k]
                    //TODO Cannot round NaN value (roundToInt)
                    val diffX = abs(scaledX.roundToInt() - scaledX)
                    val diffY = abs(scaledY.roundToInt() - scaledY)

                    if (diffX < 0.15 / ascendingScales[k]){
                        scaleInlierCountsX[k]++
                        inlierMaskX[k][i][j] = true
                    }
                    if (diffY < 0.15 / ascendingScales[k]){
                        scaleInlierCountsY[k]++
                        inlierMaskY[k][i][j] = true
                    }
                }
            }
        }
    }

    val (bestXScaleIndex, bestYScaleIndex) = try {
        // What is this shit: Finds the max in the list, then finds the max in the list of values within -10% of the max value. Surely this always returns the max value?
        Pair(scaleInlierCountsX.indices.filter {scaleInlierCountsX[it] > 0.9 * scaleInlierCountsX.maxOrNull()!! }.maxByOrNull { scaleInlierCountsX[it] }!!,
            scaleInlierCountsY.indices.filter {scaleInlierCountsY[it] > 0.9 * scaleInlierCountsY.maxOrNull()!! }.maxByOrNull { scaleInlierCountsY[it] }!!)
    } catch (e: NullPointerException){
        throw RANSACException("Could not find inliers")
    }

    val rowInlierCounts = mutableMapOf<Int, Int>()
    val colInlierCounts = mutableMapOf<Int, Int>()
    for (i in warpedPoints.indices){
        for (j in warpedPoints[i].indices){
            if (inlierMaskX[bestXScaleIndex][i][j] && inlierMaskY[bestYScaleIndex][i][j]){
                rowInlierCounts[i] = (rowInlierCounts[i] ?: 0) + 1
                colInlierCounts[j] = (colInlierCounts[j] ?: 0) + 1
            }
        }
    }
    if (rowInlierCounts.isEmpty() || colInlierCounts.isEmpty()){
        throw RANSACException("Not enough information")
    }

    val rowsToKeep = rowInlierCounts.filter { it.value.toFloat() / colInlierCounts.size.toFloat() > 0.5 }.keys
    val colsToKeep = colInlierCounts.filter { it.value.toFloat() / rowInlierCounts.size.toFloat() > 0.5 }.keys

    return Pair(Pair(rowsToKeep,colsToKeep), Pair(ascendingScales[bestXScaleIndex], ascendingScales[bestYScaleIndex]))
}

private fun quantizePoints(warpedScaledPoints: Array<Array<Point?>>, intersectionPoints: Array<Array<Point?>>): RANSACResults{
    val xSumAlongCols = mutableMapOf<Int, Double>()
    val ySumAlongRows = mutableMapOf<Int, Double>()
    for (i in warpedScaledPoints.indices){
        for (j in warpedScaledPoints[i].indices){
            warpedScaledPoints[i][j]?.let{
                // This is summing over all i
                xSumAlongCols[j] = (xSumAlongCols[j] ?: 0.0) + it.x
                // This is summing over all j
                ySumAlongRows[i] = (ySumAlongRows[i] ?: 0.0) + it.y
            }
        }
    }

    var colXs = xSumAlongCols.map { (it.value / warpedScaledPoints.size.toDouble()).roundToInt() }
    var rowYs = ySumAlongRows.map { (it.value / warpedScaledPoints[0].size.toDouble()).roundToInt() }

    val distinctCols = colXs.distinctIndexed()
    val distinctRows = rowYs.distinctIndexed()

    var filteredIntersectionPoints = intersectionPoints
        .sliceArray(distinctRows.first)
        .map { it.sliceArray(distinctCols.first) }.toTypedArray()

    colXs = distinctCols.second
    rowYs = distinctRows.second

    var xMin = colXs.minOrNull() ?: run { throw RANSACException("Not enough information") }
    var xMax = colXs.maxOrNull() ?: run { throw RANSACException("Not enough information") }
    var yMin = rowYs.minOrNull() ?: run { throw RANSACException("Not enough information") }
    var yMax = rowYs.maxOrNull() ?: run { throw RANSACException("Not enough information") }


    while (xMax - xMin > 9){
        xMax -= 1
        xMin += 1
    }
    while (yMax - yMin > 9){
        yMax -= 1
        yMin += 1
    }

    val colMask = colXs.map { (it >= xMin) && (it <= xMax) }
    val rowMask = rowYs.map { (it >= yMin) && (it <= yMax) }

    colXs = colXs.filterIndexed { index, _ -> colMask[index] }
    rowYs = rowYs.filterIndexed { index, _ -> rowMask[index] }

    filteredIntersectionPoints = filteredIntersectionPoints
        .filterIndexed { index, _ -> rowMask[index] }
        .map { it.filterIndexed { index, _ -> colMask[index] }.toTypedArray() }.toTypedArray()

    val quantizedPointGrid = meshGrid(colXs.map { it.toFloat() }.toFloatArray(), rowYs.map { it.toFloat() }.toFloatArray() )
    val quantizedPoints = Array(quantizedPointGrid.first.size) {Array<Point?>(quantizedPointGrid.first[0].size) { null } }

    for (i in quantizedPointGrid.first.indices){
        for (j in quantizedPointGrid.first[i].indices){
            quantizedPointGrid.first[i][j] -= (xMin - 5f)
            quantizedPointGrid.first[i][j] *= 50f

            quantizedPointGrid.second[i][j] -= (yMin - 5f)
            quantizedPointGrid.second[i][j] *= 50f

            quantizedPoints[i][j] = Point(quantizedPointGrid.first[i][j].toDouble(), quantizedPointGrid.second[i][j].toDouble())
        }
    }

    xMax = xMax-xMin+5
    yMax = yMax-yMin+5
    val warpedImageSize = Size(50.0 * (xMax + 5), 50.0 * (yMax + 5))

    return RANSACResults(
        xMax= xMax, yMax = yMax,
        quantizedPoints = quantizedPoints,
        intersectionPoints = filteredIntersectionPoints,
        warpedImageSize = warpedImageSize
    )
}

fun runRANSAC(intersectionPoints: Array<Array<Point?>>): RANSACResults? {
    var bestNumInliers = 0
    var bestRansacConfig: RANSACResults? = null
    var epoch = 0
    while (bestNumInliers < 30 || epoch < 200) {

        val rowIndices = intersectionPoints.indices.shuffled().take(2).sorted()
        val colIndices = intersectionPoints[0].indices.shuffled().take(2).sorted()

        val homographyMat = findHomography(
            MatOfPoint2f(
                intersectionPoints[rowIndices[0]][colIndices[0]],
                intersectionPoints[rowIndices[0]][colIndices[1]],
                intersectionPoints[rowIndices[1]][colIndices[1]],
                intersectionPoints[rowIndices[1]][colIndices[0]],
            ),
            MatOfPoint2f(
                Point(0.0, 0.0), Point(1.0, 0.0),
                Point(1.0, 1.0), Point(0.0, 1.0)
            )
        )

        // Warp all other points using this homography matrix
        var warpedPoints = warpPoints(intersectionPoints, homographyMat)

        // Count how many inliers there are for variety of scales, choose scales with most inliers
        val (rowsAndColsToKeep, scales) = try {
            discardOutliers(warpedPoints)
        } catch (e: RANSACException) {
            continue
        }

        warpedPoints = warpedPoints
            .sliceArray(rowsAndColsToKeep.first)
            .map { it.sliceArray(rowsAndColsToKeep.second) }.toTypedArray()

        val filteredIntersectionPoints = intersectionPoints
            .sliceArray(rowsAndColsToKeep.first)
            .map { it.sliceArray(rowsAndColsToKeep.second) }.toTypedArray()

        var numInliers = try { warpedPoints.size * warpedPoints[0].size } catch (e: IndexOutOfBoundsException) { continue }
        if (numInliers > bestNumInliers){
            scalePoints(warpedPoints, xScale = scales.first, yScale = scales.second)
            val ransacConfig = quantizePoints(warpedScaledPoints = warpedPoints, intersectionPoints = filteredIntersectionPoints)

            numInliers = try{
                ransacConfig.quantizedPoints.size *
                        ransacConfig.quantizedPoints[0].size
            } catch (e: IndexOutOfBoundsException){ continue }

            if (numInliers > bestNumInliers){
                bestNumInliers = numInliers
                bestRansacConfig = ransacConfig
            }
        }
        epoch++
        if (epoch > 1000){
            break
        }
    }
    return bestRansacConfig
}

private fun scalePoints(points: Array<Array<Point?>>, xScale: Int, yScale: Int){
    for (i in points.indices){
        for (j in points[i].indices){
            points[i][j]?.let {
                points[i][j] = Point(xScale * it.x, yScale * it.y)
            }
        }
    }
}

fun <T> List<T>.distinctIndexed(): Pair<List<Int>, List<T>> =
    this.withIndex().distinctBy { it.value }
        .let { indexedDistinctVals ->
            Pair(
                indexedDistinctVals.map{ it.index },
                indexedDistinctVals.map { it.value }
            )
        }

class RANSACException(message: String): Exception(message)

data class RANSACResults(
    val xMin: Int = 5, val xMax: Int, val yMin: Int = 5, val yMax: Int,
    val scale: Pair<Int, Int> = Pair(50, 50),
    val quantizedPoints: Array<Array<Point?>>,
    val intersectionPoints: Array<Array<Point?>>,
    val warpedImageSize: Size
    ){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RANSACResults

        if (xMin != other.xMin) return false
        if (xMax != other.xMax) return false
        if (yMin != other.yMin) return false
        if (yMax != other.yMax) return false
        if (scale != other.scale) return false
        if (!quantizedPoints.contentEquals(other.quantizedPoints)) return false
        if (!intersectionPoints.contentDeepEquals(other.intersectionPoints)) return false
        if (warpedImageSize != other.warpedImageSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = xMin
        result = 31 * result + xMax
        result = 31 * result + yMin
        result = 31 * result + yMax
        result = 31 * result + scale.hashCode()
        result = 31 * result + quantizedPoints.contentDeepHashCode()
        result = 31 * result + intersectionPoints.contentDeepHashCode()
        result = 31 * result + warpedImageSize.hashCode()
        return result
    }

}