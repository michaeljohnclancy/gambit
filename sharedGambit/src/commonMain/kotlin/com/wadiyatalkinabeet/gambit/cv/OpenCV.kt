package com.wadiyatalkinabeet.gambit.cv

import com.wadiyatalkinabeet.gambit.math.algorithms.quickSelect
import com.wadiyatalkinabeet.gambit.math.datastructures.Line
import com.wadiyatalkinabeet.gambit.math.datastructures.Point
import com.wadiyatalkinabeet.gambit.math.datastructures.Segment
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

expect fun initOpenCV()

const val COLOR_BGR2GRAY = 6
const val CV_8UC1 = 0

expect open class Mat()

expect fun Mat.reshape(cn: Int, rows: Int): Mat

expect operator fun Mat.get(row: Int, column: Int): DoubleArray?

expect fun Mat.width(): Int

expect fun Mat.height(): Int

expect fun Mat.size(): Size

fun Mat.rows() = width()

fun Mat.cols() = height()

expect class MatOfPoint2f: Mat

expect class MatOfPoint3f: Mat

expect class Point(x: Double, y: Double)

expect class Point3(x: Double, y: Double, z: Double)

expect class Size(width: Double, height: Double)

expect fun multiply(src1: Mat, src2: Mat, dst: Mat)

expect fun gemm(src1: Mat, src2: Mat, alpha: Double, src3: Mat, beta: Double, dst: Mat)

expect fun vector_Point2d_to_Mat(points: List<org.opencv.core.Point>): Mat

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

fun Mat.median(): Int {
    //TODO Performance: Sorting is unnecessary for median calculation.
    val flattenedArray = ravel()
    return quickSelect(flattenedArray, 0, flattenedArray.size-1, flattenedArray.size/2)
//    return NthElement().run(flattenedArray.map { it.toDouble() }.toDoubleArray(), flattenedArray.size/2)
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

fun Mat.ravel(): IntArray {
    val reshapedMat: Mat = this.reshape(1, 1)
    val flattenedArray = IntArray(reshapedMat.width())
    for (i in flattenedArray.indices) {
        flattenedArray[i] = reshapedMat[0, i]!![0].toInt()
    }
    return flattenedArray
}

//fun linesFromHoughMat
fun Line.Companion.fromHoughLines(src: Mat): List<Line> {
    return (0 until src.rows())
        .map{ src.get(it, 0) }
        .map{
            if (it[0] > 0) {
                Line(it[0].toFloat(), it[1].toFloat())
            } else {
                Line(-it[0].toFloat(), (it[1] - PI).toFloat())
            }
        }
}

fun Point.cvToScreenCoords(screenSize: Pair<Int, Int>, matSize: Pair<Int, Int>): Point {
    return Point(matSize.second.toFloat() - y, x) *
            (screenSize.first.toFloat() / matSize.second.toFloat())
}

fun Segment.cvToScreenCoords(screenSize: Pair<Int, Int>, matSize: Pair<Int, Int>): Segment {
    return Segment(
        p0.cvToScreenCoords(screenSize, matSize),
        p1.cvToScreenCoords(screenSize, matSize)
    )
}

fun processImage(matIn: Mat): Mat {
    val matOut: Mat = Mat()
    val lowerThreshold = 10.0
    val upperThreshold = 50.0
    val apertureSize = 5
    canny(matIn, matOut, lowerThreshold, upperThreshold, apertureSize)
    return matOut
}