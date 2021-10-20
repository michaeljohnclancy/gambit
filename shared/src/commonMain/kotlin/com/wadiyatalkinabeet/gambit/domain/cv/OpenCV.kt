package com.wadiyatalkinabeet.gambit.domain.cv

import com.wadiyatalkinabeet.gambit.math.algorithms.quickSelect
import com.wadiyatalkinabeet.gambit.math.datastructures.Line
import com.wadiyatalkinabeet.gambit.math.datastructures.Segment
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

expect fun initOpenCV()

const val COLOR_BGR2GRAY = 6
const val CV_8UC1 = 0
const val CV_64F = 6

expect open class Mat(){
    operator fun get(row: Int, column: Int): DoubleArray?

    fun size(): Size
    fun type(): Int

    fun width(): Int
    fun height(): Int

    fun rows(): Int
    fun cols(): Int

    fun reshape(cn: Int, rows: Int): Mat
}

expect class MatOfPoint2f(vararg points: Point): Mat

expect class MatOfPoint3f(vararg points: Point3): Mat

expect class Point(x: Double, y: Double)

expect class Point3(x: Double, y: Double, z: Double)

expect class Size(width: Double, height: Double)

expect fun multiply(src1: Mat, src2: Mat, dst: Mat)

expect fun gemm(src1: Mat, src2: Mat, alpha: Double, src3: Mat, beta: Double, dst: Mat)

expect fun vector_Point2d_to_Mat(points: List<Point>): Mat

expect fun loadChessboardExampleImage(): Mat

expect fun cvtColor(src: Mat, dst: Mat, colorOut: Int)

expect fun resize(src: Mat, dst: Mat, dsize: Size)

expect fun canny(src: Mat, dst: Mat, lowerThreshold: Double, upperThreshold: Double)

expect fun canny(src: Mat, dst: Mat, lowerThreshold: Double, upperThreshold: Double, apertureSize: Int)

expect fun houghLines(src: Mat, lines: Mat, rho: Double, theta: Double, threshold: Int)

expect fun houghLinesP(src: Mat, lines: Mat, rho: Double, theta: Double, threshold: Int, minLineLength: Double, maxLineGap: Double)

expect fun findHomography(srcPoints: MatOfPoint2f, dstPoints: MatOfPoint2f): Mat

expect fun warpPerspective(src: Mat, dst: Mat, transformationMatrix: Mat, dsize: Size)

expect fun sobel(src: Mat, dst: Mat, ddepth: Int, dx: Int, dy: Int, kernelSize: Int)

expect fun medianBlur(src: Mat, dst: Mat, kernelSize: Int)

expect fun gaussianBlur(src: Mat, dst: Mat, kernelSize: Size, sigmaX: Double)

fun Mat.median(): Int {
    //TODO Could merge these flatten and median functions together? What runtime can be achieved?
    val flattenedArray = ravel()
    return quickSelect(flattenedArray, 0, flattenedArray.size-1, flattenedArray.size/2)
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

fun processImage(matIn: Mat): Mat {
    val matOut: Mat = Mat()
    val lowerThreshold = 10.0
    val upperThreshold = 50.0
    val apertureSize = 5
    canny(matIn, matOut, lowerThreshold, upperThreshold, apertureSize)
    return matOut
}