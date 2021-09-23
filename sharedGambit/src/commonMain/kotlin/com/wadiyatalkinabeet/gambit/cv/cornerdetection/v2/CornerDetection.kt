package com.wadiyatalkinabeet.gambit.cv.cornerdetection.v2

import com.wadiyatalkinabeet.gambit.cv.*
import com.wadiyatalkinabeet.gambit.math.datastructures.Line
import com.wadiyatalkinabeet.gambit.math.datastructures.Segment
import com.wadiyatalkinabeet.gambit.math.statistics.clustering.FCluster
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
    edges: Mat,
    eliminateDiagonalThresholdRads: Double? = null,
): List<Line> {
    val tmpMat = Mat()
    houghLines(edges, tmpMat, 1.0, PI/360, 100)
    val allLines = Line.fromHoughLines(tmpMat)

    // Looks like this would break diagonal views if enabled
    fun diagonalFilter(line: Line) = eliminateDiagonalThresholdRads?.let { thresh ->
        (abs(line.theta) < thresh || abs(line.theta - (PI / 2)) < thresh )
    } ?: true

    return allLines.filter(::diagonalFilter)
}

//TODO This needs to use HCluster, not FCluster
fun cluster(lines: List<Line>, maxAngle: Double = PI/180): Pair<List<Line>, List<Line>>? {
    val a = FCluster.apply(lines.size) { index1, index2 ->
        lines[index1].angleTo(lines[index2]) <= maxAngle
    }
    val allClusters =  a.map { cluster ->
        cluster?.map {
            it?.let { lines[it] } ?: return null
        } ?: return null
    }
    if (allClusters.size < 2)
        return null

    return allClusters.sortedBy{-it.size}.take(2).let{Pair(it[0], it[1])}
}

fun findCorners(src: Mat): Pair<List<Line>, List<Line>> {
    val tmpMat = Mat()
    resize(src, tmpMat)
    cvtColor(tmpMat, tmpMat, COLOR_BGR2GRAY)
    detectEdges(tmpMat, tmpMat)
    val allLines = detectLines(tmpMat)

    if (allLines.size > 400){
        throw InvalidFrameException("Too many lines in image")
    }

    return cluster(allLines) ?: throw InvalidFrameException("Not enough line clusters found")
}