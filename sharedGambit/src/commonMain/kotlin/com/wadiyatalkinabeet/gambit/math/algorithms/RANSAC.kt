package com.wadiyatalkinabeet.gambit.math.algorithms

import com.wadiyatalkinabeet.gambit.cv.Mat
import com.wadiyatalkinabeet.gambit.cv.Point
import com.wadiyatalkinabeet.gambit.cv.Size
import com.wadiyatalkinabeet.gambit.cv.cornerdetection.v2.InvalidFrameException
import java.lang.NullPointerException
import kotlin.math.abs
import kotlin.math.roundToInt


private fun warpPoint(x: Double, y: Double, z: Double, transformationMatrix: Mat): Triple<Double, Double, Double> {

    val warpedX = (x * transformationMatrix.get(0,0)[0]) + (y * transformationMatrix.get(0,1)[0]) + (z * transformationMatrix.get(0,2)[0])
    val warpedY = (x * transformationMatrix.get(1,0)[0]) + (y * transformationMatrix.get(1,1)[0]) + (z * transformationMatrix.get(1,2)[0])
    val warpedZ = (x * transformationMatrix.get(2,0)[0]) + (y * transformationMatrix.get(2,1)[0]) + (z * transformationMatrix.get(2,2)[0])

    return Triple(warpedX, warpedY, warpedZ)
}

fun warpPoints(intersectionPoints: List<List<Point?>>, transformationMatrix: Mat): Array<Array<Point?>>{
    val warpedPoints = Array(intersectionPoints.size) {
        Array<Point?>(
            intersectionPoints[0].size
        ) {null}
    }
    for (i in intersectionPoints.indices){
        for (j in intersectionPoints[i].indices){
            intersectionPoints[i][j]?.let {
                val (warpedX, warpedY, _) = warpPoint(it.x, it.y, 1.0, transformationMatrix)
                warpedPoints[i][j] = Point(warpedX, warpedY)
            }
        }
    }
    return warpedPoints
}

fun warpPoints(intersectionPoints: List<Point?>, transformationMatrix: Mat): Array<Point?>{
    val warpedPoints = Array<Point?>( intersectionPoints.size ) {null}

    for (i in intersectionPoints.indices){
            intersectionPoints[i]?.let {
                val (warpedX, warpedY, _) = warpPoint(it.x, it.y, 1.0, transformationMatrix)
                warpedPoints[i] = Point(warpedX, warpedY)
            }
        }
    return warpedPoints
}

fun discardOutliers(warpedPoints: List<List<Point?>>, ascendingScales: IntArray = intArrayOf(1,2,3,4,5,6,7,8)): Pair<Pair<MutableSet<Int>, MutableSet<Int>>, Pair<Int, Int>> {
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

    val rowsToConsider = mutableSetOf<Int>()
    val colsToConsider = mutableSetOf<Int>()
    val inlierMask = Array(warpedPoints.size){ Array(warpedPoints[0].size){false} }
    for (i in warpedPoints.indices){
        for (j in warpedPoints[i].indices){
            inlierMask[i][j] = inlierMaskX[bestXScaleIndex][i][j] && inlierMaskY[bestYScaleIndex][i][j]
            if(inlierMask[i][j]){
                rowsToConsider.add(i)
                colsToConsider.add(j)
            }
        }
    }
    val numColsToConsider = colsToConsider.size
    val numRowsToConsider = rowsToConsider.size

    if (numRowsToConsider == 0 || numColsToConsider == 0){
        throw RANSACException("Not enough information")
    }

    val rowCounts = mutableMapOf<Int, Int>()
    val colCounts = mutableMapOf<Int, Int>()
    for (i in inlierMask.indices){
        for (j in inlierMask[i].indices){
            val hasInlier = if (inlierMask[i][j]) 1 else 0
            rowCounts[i] = (rowCounts[i] ?: 0) + hasInlier
            colCounts[j] = (colCounts[j] ?: 0) + hasInlier
        }
    }

    val rowsToKeep = mutableSetOf<Int>()
    val colsToKeep = mutableSetOf<Int>()
    for ((colIndex, columnCount) in colCounts){
        if (columnCount / numRowsToConsider > 0.5){
            colsToKeep.add(colIndex)
        }
    }
    for ((rowIndex, rowCount) in rowCounts){
        if (rowCount / numColsToConsider > 0.5){
            rowsToKeep.add(rowIndex)
        }
    }

    return Pair(Pair(rowsToKeep,colsToKeep), Pair(ascendingScales[bestXScaleIndex], ascendingScales[bestYScaleIndex]))
}

fun quantizePoints(warpedScaledPoints: List<List<Point?>>, intersectionPoints: List<List<Point?>>): RANSACConfiguration{
    val rowSumYs = MutableList(warpedScaledPoints.size){ 0.0 }
    val colSumXs = MutableList(warpedScaledPoints[0].size){ 0.0 }
    for (i in warpedScaledPoints.indices){
        for (j in warpedScaledPoints[i].indices){
            warpedScaledPoints[i][j]?.let{
                colSumXs[j] += it.x
                rowSumYs[i] += it.y
            }
        }
    }

    var colXs = colSumXs.map { (it / warpedScaledPoints[0].size).toInt() }
    var rowYs = rowSumYs.map { (it / warpedScaledPoints.size).toInt() }

    val distinctCols = colXs.distinctIndexed()
    val distinctRows = rowYs.distinctIndexed()

    colXs = distinctCols.first
    rowYs = distinctRows.first

    var filteredIntersectionPoints = intersectionPoints
        .filterIndexed { index, _ -> distinctRows.second.contains(index) }
        .map { it.filterIndexed { index, _ -> distinctCols.second.contains(index) } }

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
    rowYs = rowYs.filterIndexed {  index, _ -> rowMask[index] }

    filteredIntersectionPoints = filteredIntersectionPoints
        .filterIndexed { index, _ -> rowMask[index] }
        .map { it.filterIndexed { index, _ -> colMask[index] } }

    var quantizedPoints = meshGrid(colXs.map { it.toFloat() }.toFloatArray(), rowYs.map { it.toFloat() }.toFloatArray() )

    for (i in quantizedPoints.first.indices){
        for (j in quantizedPoints.first[i].indices){
            quantizedPoints.first[i][j] -= (xMin - 5f)
            quantizedPoints.first[i][j] *= 50f

            quantizedPoints.second[i][j] -= (yMin - 5f)
            quantizedPoints.second[i][j] *= 50f
        }

    }

    xMax = xMax-xMin+5
    yMax = yMax-yMin+5
    val warpedImageSize = Size(50.0*(xMax + 5), 50.0 * (yMax + 5))

    return RANSACConfiguration(
        xMax= xMax, yMax = yMax,
        scaledQuantizedPoints = quantizedPoints,
        intersectionPoints = filteredIntersectionPoints,
        warpedImageSize = warpedImageSize
    )

}

fun <T> List<T>.distinctIndexed(): Pair<List<T>, List<Int>> = this.withIndex().distinctBy { it.value }.let { indexedVal -> Pair(indexedVal.map { it.value }, indexedVal.map{ it.index }) }

class RANSACException(message: String): Exception(message)

data class RANSACConfiguration(
    val xMin: Int = 5, val xMax: Int, val yMin: Int = 5, val yMax: Int,
    val scale: Pair<Int, Int> = Pair(50, 50),
    val scaledQuantizedPoints: Pair<Array<FloatArray>, Array<FloatArray>>,
    val intersectionPoints: List<List<Point?>>,
    val warpedImageSize: Size
    ){

}