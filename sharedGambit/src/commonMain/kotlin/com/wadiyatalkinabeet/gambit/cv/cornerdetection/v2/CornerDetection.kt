package com.wadiyatalkinabeet.gambit.cv.cornerdetection.v2

import com.wadiyatalkinabeet.gambit.cv.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

fun resize(
    src: Mat,
    dst: Mat,
    numPixels: Float = 500f.pow(2f),
): Double {
    val w: Double = src.width().toDouble()
    val h: Double = src.height().toDouble()
    val scale: Double = sqrt(numPixels / (w * h))
    resize(src, dst, Size(w * scale, h * scale))
    return scale
}

fun detectEdges(src: Mat, edges: Mat){
    //Needs to be uint8 according to python?
    canny(src, edges, 90.0, 400.0, 3)
}

fun detectLines(
    edges: Mat, lines: Mat,
    eliminateDiagonalThresholdRads: Double? = null,
){
    val tmpMat = Mat()
    houghLines(edges, tmpMat, 1.0, PI/360, 100)
    fixNegativeRhoInHesseNormalForm(tmpMat, tmpMat)

    fun diagonalFilter(line: DoubleArray) = eliminateDiagonalThresholdRads?.let {
        (abs(line[1]) < it || abs(line[1] - (PI / 2)) < it )
    } ?: true

    (0 until tmpMat.rows())
        .map {tmpMat.get(it, 0) }
        .filter(::diagonalFilter)
        .filter { it[0] < 0 }
        .forEach {
            it[0] = -it[0]
            it[1] = it[1] - PI
        }
    }

fun fixNegativeRhoInHesseNormalForm(linesIn: Mat, linesOut: Mat){
}

fun findCorners(src: Mat){
    val tmpMat = Mat()
    resize(src, tmpMat)
    cvtColor(tmpMat, tmpMat, COLOR_BGR2GRAY)
    detectEdges(tmpMat, tmpMat)
    detectLines(tmpMat, tmpMat)

    if (tmpMat.rows() > 400){
        throw InvalidFrameException("Too many lines in image")
    }






}