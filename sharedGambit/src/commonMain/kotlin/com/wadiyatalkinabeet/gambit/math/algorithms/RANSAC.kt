package com.wadiyatalkinabeet.gambit.math.algorithms

import com.wadiyatalkinabeet.gambit.cv.Mat
import com.wadiyatalkinabeet.gambit.cv.Point
import com.wadiyatalkinabeet.gambit.cv.cornerdetection.v2.InvalidFrameException
import org.jetbrains.kotlinx.multik.api.toNDArray
import java.lang.NullPointerException
import kotlin.math.abs
import kotlin.math.roundToInt


private fun warpPoint(x: Double, y: Double, z: Double, transformationMatrix: Mat): Triple<Double, Double, Double> {

    val warpedX = (x * transformationMatrix.get(2,0)[0]) + (y * transformationMatrix.get(2,1)[0]) + (z * transformationMatrix.get(0,2)[0])
    val warpedY = (x * transformationMatrix.get(1,0)[0]) + (y * transformationMatrix.get(1,1)[0]) + (z * transformationMatrix.get(1,2)[0])
    val warpedZ = (x * transformationMatrix.get(0,0)[0]) + (y * transformationMatrix.get(0,1)[0]) + (z * transformationMatrix.get(0,2)[0])

    return Triple(warpedX, warpedY, warpedZ)
}

fun warpPoints(intersectionPoints: Array<Array<Point?>>, transformationMatrix: Mat): Array<Array<Point?>>{
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

fun discardOutliers(warpedPoints: Array<Array<Point?>>, intersectionPoints: Array<Array<Point?>>){
}

fun findBestScale(warpedPoints: Array<Array<Point?>>, ascendingScales: IntArray = intArrayOf(1,2,3,4,5,6,7,8)): Triple<Int, Int, Array<Array<Boolean>>> {
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

    val (bestXScaleIndex, bestYScaleIndex) = try{
        // What is this shit: Finds the max in the list, then finds the max in the list of values within -10% of the max value. Surely this always returns the max value?
        Pair(scaleInlierCountsX.indices.filter {scaleInlierCountsX[it] > 0.9 * scaleInlierCountsX.maxOrNull()!! }.maxByOrNull { scaleInlierCountsX[it] }!!,
            scaleInlierCountsY.indices.filter {scaleInlierCountsY[it] > 0.9 * scaleInlierCountsX.maxOrNull()!! }.maxByOrNull { scaleInlierCountsY[it] }!!)
    } catch (e: NullPointerException){
        throw InvalidFrameException("RANSAC: Could not find inliers")
    }

    val inlierMask = Array(warpedPoints.size){ Array(warpedPoints[0].size){false} }
    for (i in warpedPoints.indices){
        for (j in warpedPoints[i].indices){
            inlierMask[i][j] = inlierMaskX[bestXScaleIndex][i][j] && inlierMaskY[bestYScaleIndex][i][j]
        }
    }

    return Triple(ascendingScales[bestXScaleIndex], ascendingScales[bestYScaleIndex], inlierMask)
}
