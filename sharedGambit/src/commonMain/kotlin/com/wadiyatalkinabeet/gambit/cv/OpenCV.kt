package com.wadiyatalkinabeet.gambit.cv

import com.wadiyatalkinabeet.gambit.math.geometry.Segment
import com.wadiyatalkinabeet.gambit.math.statistics.median
import kotlin.math.max
import kotlin.math.min


const val COLOR_BGR2GRAY = 6

expect open class Mat()

expect fun Mat.reshape(cn: Int, rows: Int): Mat

expect operator fun Mat.get(row: Int, column: Int): DoubleArray?

expect fun Mat.width(): Int

expect fun Mat.height(): Int

expect fun Mat.size(): Size

fun Mat.rows() = width()

fun Mat.cols() = height()

expect class MatOfPoint2f: Mat

expect class Size(width: Double, height: Double)

expect fun loadChessboardExampleImage(): Mat

expect fun cvtColor(src: Mat, dst: Mat, colorOut: Int)

expect fun resize(src: Mat, dst: Mat, dsize: Size)

expect fun canny(src: Mat, dst: Mat, lowerThreshold: Double, upperThreshold: Double)

expect fun canny(src: Mat, dst: Mat, lowerThreshold: Double, upperThreshold: Double, apertureSize: Int)

expect fun houghLines(src: Mat, lines: Mat, rho: Double, theta: Double, threshold: Int)

expect fun houghLinesP(src: Mat, lines: Mat, rho: Double, theta: Double, threshold: Int, minLineLength: Double, maxLineGap: Double)

expect fun findHomography(srcPoints: MatOfPoint2f, dstPoints: MatOfPoint2f): Mat

expect fun warpPerspective(src: Mat, dst: Mat, transformationMatrix: Mat, dsize: Size)

expect fun sobel(src: Mat, dst: Mat, ddepth: Int, dx: Int, dy: Int)

expect fun medianBlur(src: Mat, dst: Mat, kernelSize: Int)

expect fun gaussianBlur(src: Mat, dst: Mat, kernelSize: Size, sigmaX: Double)

fun Mat.median(): Double {
    //TODO Performance: Sorting is unnecessary for median calculation.
    val flattenedArray = ravel()
    flattenedArray.sort()
    return median(flattenedArray)
}


fun autoCanny(
    src: Mat,
    dst: Mat,
    sigma: Double = 0.25
){
    val median = src.median()
    medianBlur(src, dst, 5)
    gaussianBlur(dst, dst, Size(7.0, 7.0), 2.0)
    val lowerThreshold: Double = max(0.0, (1.0 - sigma) * median)
    val upperThreshold: Double = min(255.0, (1.0 + sigma) * median)
    canny(dst, dst, lowerThreshold, upperThreshold)
}

fun Mat.ravel(): DoubleArray {
    val reshapedMat: Mat = this.reshape(1, 1)
    val flattenedArray = DoubleArray(reshapedMat.width())
    for (i in flattenedArray.indices) {
        flattenedArray[i] = reshapedMat[0, i]!![0]
    }
    return flattenedArray
}

fun processImage(matIn: Mat): Mat {
    val matOut: Mat = Mat()
    val lowerThreshold = 10.0
    val upperThreshold = 50.0
    val apertureSize = 5
    canny(matIn, matOut, lowerThreshold, upperThreshold, apertureSize)
    return matOut
}