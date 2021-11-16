package com.wadiyatalkinabeet.gambit.domain.cv.cornerdetection.v2

import com.wadiyatalkinabeet.gambit.domain.cv.*
import com.wadiyatalkinabeet.gambit.domain.cv.Point
import com.wadiyatalkinabeet.gambit.domain.math.algorithms.RANSACResults
import com.wadiyatalkinabeet.gambit.domain.math.algorithms.warpPoint
import com.wadiyatalkinabeet.gambit.domain.math.datastructures.inverse
import com.wadiyatalkinabeet.gambit.domain.math.datastructures.toMat
import com.wadiyatalkinabeet.gambit.domain.math.datastructures.toMatrix
import com.wadiyatalkinabeet.gambit.domain.cv.CV_32FC1

fun detectCorners(
    ransacResults: RANSACResults,
    sourceMat: Mat,
    scale: Double
): List<com.wadiyatalkinabeet.gambit.domain.math.datastructures.Point>? {

    if (ransacResults.intersectionPoints.size * ransacResults.intersectionPoints[0].size < 4) {
        return null
    }

    val transformationMat = findHomography(
        MatOfPoint2(ransacResults.intersectionPoints.flatten().filterNotNull()),
        MatOfPoint2(ransacResults.quantizedPoints.flatten().filterNotNull())
    )

    val warpedGrayscaleMat = Mat()
    warpPerspective(
        sourceMat, warpedGrayscaleMat,
        transformationMat, ransacResults.warpedImageSize
    )

    val warpedBordersMat = Mat()
    warpPerspective(
        makeBorderMat(size = sourceMat.size()),
        warpedBordersMat, transformationMat, ransacResults.warpedImageSize
    )

    val (xLims, yLims) = try {
        expandLimits(
            warpedGrayscaleMat, warpedBordersMat, ransacResults.scale,
            ransacResults.xMin, ransacResults.xMax, ransacResults.yMin, ransacResults.yMax
        )
    } catch (e: ImageProcessingException) {
        return null
    }
    val (xMin, xMax) = xLims
    val (yMin, yMax) = yLims

    val inverseWarpMatrix = transformationMat.toMatrix().inverse().toMat()

    return listOf(
        warpPoint(
            Point(
                (ransacResults.scale.first * xMin).toFloat(),
                (ransacResults.scale.second * yMin).toFloat()),
            inverseWarpMatrix
        ),
        warpPoint(
            Point(
                (ransacResults.scale.first * xMax).toFloat(),
                (ransacResults.scale.second * yMin).toFloat()),
            inverseWarpMatrix
        ),
        warpPoint(
            Point(
                (ransacResults.scale.first * xMax).toFloat(),
                (ransacResults.scale.second * yMax).toFloat()),
            inverseWarpMatrix
        ),
        warpPoint(
            Point(
                (ransacResults.scale.first * xMin).toFloat(),
                (ransacResults.scale.second * yMax).toFloat()),
            inverseWarpMatrix
        ),
    ).map {
        com.wadiyatalkinabeet.gambit.domain.math.datastructures.Point(
            it.x, it.y
        )
    }
}

fun Mat.expandLimits(verticals: Boolean, scale: Int, min: Int, max: Int): Pair<Int, Int> {
    val cachedSums = mutableMapOf<Int, Int>()
    fun calcSum(i: Int): Int {
        val x = i * scale
        var sum = 0
        for (i in if (verticals) { 0 until rows() } else { x-2 until x+3 } ){
            for (j in if (verticals) { x-2 until x+3 } else { 0 until cols()} ){
                sum += if (this[i,j][0] == 0f) { 0 } else { 1 }
            }
        }
        return sum
    }
    fun getSum(i: Int) = cachedSums.getOrPut(i, { calcSum(i) })

    var newMin = min
    var newMax = max
    val lastIndex = if (verticals) { width() } else { height() } / scale - 2
    while (newMax - newMin < 8) {
        if (newMax > lastIndex) {
            newMin -= 1
            continue
        }
        if (newMin < 2) {
            newMax += 1
            continue
        }
        val down = getSum(newMin - 1)
        val up = getSum(newMax + 1)
        if (down > up) {
            newMin -= 1
        } else {
            newMax += 1
        }
    }
    return Pair(newMin, newMax)
}

private fun expandLimits(
    warpedGrayscaleMat: Mat, warpedBorderMat: Mat,
    scale: Pair<Int, Int>,
    xMin: Int, xMax: Int, yMin: Int, yMax: Int
): Pair<Pair<Int, Int>, Pair<Int, Int>> {
    val horizontalsMat = Mat()
    val verticalsMat = Mat()
    sobel(warpedGrayscaleMat, horizontalsMat, CV_32FC1, 0, 1, 3)
    sobel(warpedGrayscaleMat, verticalsMat, CV_32FC1, 1, 0, 3)
    listOf(horizontalsMat, verticalsMat).forEach { mat ->
        convertScaleAbs(mat, mat)
        mat.convertTo(mat, CV_8UC1)
        canny(mat, mat, 120.0, 300.0, 3)
        mat.convertTo(mat, CV_32FC1)
        mat.applyMask(warpedBorderMat)
    }
    val (xMinNew, xMaxNew) = verticalsMat.expandLimits(true, scale.first, xMin, xMax)
    val (yMinNew, yMaxNew) = horizontalsMat.expandLimits(false, scale.second, yMin, yMax)

    return Pair(Pair(xMinNew, xMaxNew), Pair(yMinNew, yMaxNew))
}

fun makeBorderMat(size: Size, type: Int = CV_32FC1, borderThickness: Int = 3): Mat{
    val bordersMat = Mat.zeros(size, type)
    for (i in borderThickness until bordersMat.rows()-borderThickness){
        for (j in borderThickness until bordersMat.cols()-borderThickness){
            bordersMat[i, j] = floatArrayOf(255f)
        }
    }
    return bordersMat
}

class ImageProcessingException(message: String): Exception(message)