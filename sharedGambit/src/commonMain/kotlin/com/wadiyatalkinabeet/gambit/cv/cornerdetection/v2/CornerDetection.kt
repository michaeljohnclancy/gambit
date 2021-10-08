package com.wadiyatalkinabeet.gambit.cv.cornerdetection.v2

import com.wadiyatalkinabeet.gambit.cv.*
import com.wadiyatalkinabeet.gambit.cv.Point
import com.wadiyatalkinabeet.gambit.math.algorithms.*
import com.wadiyatalkinabeet.gambit.math.datastructures.inverse
import com.wadiyatalkinabeet.gambit.math.datastructures.toMat
import com.wadiyatalkinabeet.gambit.math.datastructures.toMatrix

fun detectCorners(
    ransacResults: RANSACResults,
    sourceMat: Mat,
    scale: Double
): List<com.wadiyatalkinabeet.gambit.math.datastructures.Point>? {

    if (ransacResults.intersectionPoints.size * ransacResults.intersectionPoints[0].size < 4) {
        return null
    }

    val transformationMat = findHomography(
        MatOfPoint2f(
            *ransacResults.intersectionPoints.flatten().filterNotNull().toTypedArray()
        ),
        MatOfPoint2f(*ransacResults.quantizedPoints.flatten().filterNotNull().toTypedArray())
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

    val (xMin, xMax) = try {
        computeVerticalBorders(
            warpedGrayscaleMat, warpedBordersMat, ransacResults.scale,
            ransacResults.xMin, ransacResults.xMax
        )
    } catch (e: ImageProcessingException) {
        return null
    }

    val scaledXMin = ransacResults.scale.first * xMin
    val scaledXMax = ransacResults.scale.first * xMax

    for (i in 0 until warpedBordersMat.rows()) {
        for (j in 0 until warpedBordersMat.cols()) {
            if (i < scaledXMin || i > scaledXMax) {
                warpedBordersMat.put(i, j, 0.0)
            }
        }
    }

    val (yMin, yMax) = try {
        computeHorizontalBorders(
            warpedGrayscaleMat, warpedBordersMat, ransacResults.scale,
            ransacResults.yMin, ransacResults.yMax
        )
    } catch (e: ImageProcessingException) {
        return null
    }

    val inverseWarpMatrix = transformationMat.toMatrix().inverse().toMat()

    return listOf(
        warpPoint(
            Point(
                (ransacResults.scale.first * xMin).toDouble(),
                (ransacResults.scale.second * yMin).toDouble()),
            inverseWarpMatrix
        ),
        warpPoint(
            Point(
                (ransacResults.scale.first * xMax).toDouble(),
                (ransacResults.scale.second * yMin).toDouble()),
            inverseWarpMatrix
        ),
        warpPoint(
            Point(
                (ransacResults.scale.first * xMax).toDouble(),
                (ransacResults.scale.second * yMax).toDouble()),
            inverseWarpMatrix
        ),
        warpPoint(
            Point(
                (ransacResults.scale.first * xMin).toDouble(),
                (ransacResults.scale.second * yMax).toDouble()),
            inverseWarpMatrix
        ),
    ).map {
        com.wadiyatalkinabeet.gambit.math.datastructures.Point(
            (it.x / scale).toFloat(), (it.y / scale).toFloat()
        )
    }
}

private fun computeVerticalBorders(
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

private fun computeHorizontalBorders(
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

fun makeBorderMat(size: Size, type: Int = CV_8UC1, borderThickness: Int = 3): Mat{
    val bordersMat = Mat.zeros(size, type)
    for (i in 3 until bordersMat.rows()-borderThickness){
        for (j in 3 until bordersMat.cols()-borderThickness){
            bordersMat.put(i, j, 255.0)
        }
    }
    return bordersMat
}

class ImageProcessingException(message: String): Exception(message)