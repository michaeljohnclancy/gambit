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

//TODO It could be reasonable to add the below back in once everything works

//    for (i in 0 until warpedBordersMat.rows()) {
//        for (j in 0 until warpedBordersMat.cols()) {
//            if (i < scaledXMin || i > scaledXMax) {
//                warpedBordersMat[i, j] = floatArrayOf(0f)
//            }
//        }
//    }

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

private fun computeVerticalBorders(
    warpedGrayscaleMat: Mat, warpedBorderMat: Mat,
    scale: Pair<Int, Int>, xMin: Int, xMax: Int
): Pair<Int, Int>{
    val resultMat = Mat()
    sobel(warpedGrayscaleMat, resultMat, CV_32FC1, 1, 0, 3)
    convertScaleAbs(resultMat, resultMat)

//    resultMat.convertTo(resultMat, CV_32FC1)
//    for (i in 0 until resultMat.rows()){
//        for (j in 0 until resultMat.cols()){
//            if (warpedBorderMat[i, j][0] != 255f){
//                resultMat[i, j] = floatArrayOf(0f)
//            }
//        }
//    }

    resultMat.convertTo(resultMat, CV_8UC1)
    canny(resultMat, resultMat, 120.0, 300.0, 3)
    resultMat.convertTo(resultMat, CV_32FC1)

    for (i in 0 until resultMat.rows()){
        for (j in 0 until resultMat.cols()){
            if (warpedBorderMat[i, j][0] != 255f){
                resultMat[i, j] = floatArrayOf(0f)
            }
        }
    }

    fun getSumAtCol(y: Int): Int {
        val yScaled = y * scale.second

        var sum = 0
        for (i in 0 until resultMat.rows()){
            for (j in (yScaled-2 until yScaled+3) ){
                sum += if (resultMat[i,j][0] == 0f) { 0 } else { 1 }
            }
        }
        return sum
    }

    var xMaxCorrected = xMax
    var xMinCorrected = xMin

    while (xMaxCorrected - xMinCorrected < 8 && xMinCorrected > 1 && xMaxCorrected < 17){
        val right = getSumAtCol(xMaxCorrected + 1)
        val left = getSumAtCol(xMinCorrected - 1)

        if (right > left){
            xMaxCorrected += 1
        } else{
            xMinCorrected -= 1
        }
    }

    return Pair(xMinCorrected, xMaxCorrected)
}

private fun computeHorizontalBorders(
    warpedGrayscaleMat: Mat, warpedBorderMat: Mat,
    scale: Pair<Int, Int>, yMin: Int, yMax: Int
): Pair<Int, Int>{
    val resultMat = Mat()
    sobel(warpedGrayscaleMat, resultMat, CV_32FC1, 0, 1, 3)
    convertScaleAbs(resultMat, resultMat)

//    resultMat.convertTo(resultMat, CV_32FC1)
//    for (i in 0 until resultMat.rows()){
//        for (j in 0 until resultMat.cols()){
//            if (warpedBorderMat[i, j][0] != 255f){
//                resultMat[i, j] = floatArrayOf(0f)
//            }
//        }
//    }

    resultMat.convertTo(resultMat, CV_8UC1)
    canny(resultMat, resultMat, 120.0, 300.0, 3)
    resultMat.convertTo(resultMat, CV_32FC1)

    for (i in 0 until resultMat.rows()){
        for (j in 0 until resultMat.cols()){
            if (warpedBorderMat[i, j][0] != 255f){
                resultMat[i, j] = floatArrayOf(0f)
            }
        }
    }

    fun getSumAtRow(y: Int): Int {
        val yScaled = y * scale.first

        var sum = 0
        for (i in (yScaled-2 until yScaled+3)){
            for (j in 0 until resultMat.cols()){
                sum += if (resultMat[i,j][0] == 0f) { 0 } else { 1 }
            }
        }
        return sum
    }

    var yMaxCorrected = yMax
    var yMinCorrected = yMin

    while (yMaxCorrected - yMinCorrected < 8 && yMinCorrected > 1 && yMaxCorrected < 17){
        val top = getSumAtRow(yMaxCorrected + 1)
        val bottom = getSumAtRow(yMinCorrected - 1)

        if (top > bottom){
            yMaxCorrected += 1
        } else{
            yMinCorrected -= 1
        }
    }

    return Pair(yMinCorrected, yMaxCorrected)
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