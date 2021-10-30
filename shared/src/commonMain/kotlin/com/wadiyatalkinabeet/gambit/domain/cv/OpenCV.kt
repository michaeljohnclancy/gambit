package com.wadiyatalkinabeet.gambit.domain.cv

import com.wadiyatalkinabeet.gambit.domain.math.algorithms.quickSelect
import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Line
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

const val COLOR_BGR2GRAY = 6
const val CV_8UC1 = 0
const val CV_32FC1 = 5
const val CV_64FC1 = 6

expect class Mat(){
    companion object {
        fun zeros(size: Size, type: Int): Mat
    }

    constructor(width: Int, height: Int, type: Int, byteBuffer: ByteBuffer? = null)

    operator fun get(row: Int, col: Int): FloatArray
    operator fun set(row: Int, col: Int, value: FloatArray)

    fun size(): Size
    fun type(): Int

    fun width(): Int
    fun height(): Int

    fun rows(): Int
    fun cols(): Int

    fun reshape(channels: Int, rows: Int): Mat
    fun convertTo(resultMat: Mat, type: Int)

    fun row(rowIndex: Int): Mat
    fun col(colIndex: Int): Mat
}

expect fun imread(path: String): Mat

expect class MatOfPoint2(points: List<Point>)

expect class MatOfPoint3(points: List<Point3>)

expect class Point(x: Float, y: Float){
    val x: Float
    val y: Float
}

expect class Point3(x: Float, y: Float, z: Float){
    val x: Float
    val y: Float
    val z: Float
}

expect class Size(width: Int, height: Int)

expect fun multiply(src1: Mat, src2: Mat, dst: Mat)

//expect fun gemm(src1: Mat, src2: Mat, alpha: Double, src3: Mat, beta: Double, dst: Mat)

//expect fun vector_Point2d_to_Mat(points: List<Point>): Mat

expect fun cvtColor(src: Mat, dst: Mat, colorOut: Int)

expect fun resize(src: Mat, dst: Mat, dsize: Size)

expect fun canny(src: Mat, dst: Mat, lowerThreshold: Double, upperThreshold: Double)

expect fun canny(src: Mat, dst: Mat, lowerThreshold: Double, upperThreshold: Double, apertureSize: Int)

expect fun houghLines(src: Mat, lines: Mat, rho: Double, theta: Double, threshold: Int)

expect fun houghLinesP(src: Mat, lines: Mat, rho: Double, theta: Double, threshold: Int, minLineLength: Double, maxLineGap: Double)

expect fun findHomography(srcPoints: MatOfPoint2, dstPoints: MatOfPoint2): Mat

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
    gaussianBlur(dst, dst, Size(7, 7), 2.0)
    val lowerThreshold: Double = max(0.0, (1.0 - sigma) * median)
    val upperThreshold: Double = min(255.0, (1.0 + sigma) * median)
    canny(dst, dst, lowerThreshold, upperThreshold)
}

fun Mat.ravel(): IntArray {
    val reshapedMat: Mat = this.reshape(1, 1)
    val flattenedArray = IntArray(reshapedMat.width())
    for (i in flattenedArray.indices) {
        flattenedArray[i] = reshapedMat[0, i][0].toInt()
    }
    return flattenedArray
}

//fun linesFromHoughMat
fun Line.Companion.fromHoughLines(src: Mat): List<Line> {
    return (0 until src.rows())
        .map{ src[it, 0] }
        .map{
            if (it[0] > 0) {
                Line(it[0], it[1])
            } else {
                Line(-it[0], (it[1] - PI).toFloat())
            }
        }
}
