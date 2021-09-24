package com.wadiyatalkinabeet.gambit.cv.cornerdetection.v2

import com.wadiyatalkinabeet.gambit.cv.*
import com.wadiyatalkinabeet.gambit.math.datastructures.Line
import com.wadiyatalkinabeet.gambit.math.datastructures.Point
import com.wadiyatalkinabeet.gambit.math.datastructures.Segment
import com.wadiyatalkinabeet.gambit.math.statistics.clustering.AverageAgglomerative
import com.wadiyatalkinabeet.gambit.math.statistics.clustering.DBScan
import com.wadiyatalkinabeet.gambit.math.statistics.clustering.FCluster
import java.lang.IndexOutOfBoundsException
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
//    canny(src, edges, 90.0, 400.0, 3)
    autoCanny(src, edges)
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

fun eliminateSimilarLines(lines: List<Line>, perpendicularLines: List<Line>): List<Line> {
    val perpendicular = perpendicularLines
        .fold(
            initial = Line(0.0, 0.0),
            operation = { mean: Line, line: Line ->
                Line(mean.rho + line.rho, mean.theta + line.theta)
            })
        .let{
            Line(it.rho / perpendicularLines.size, it.theta / perpendicularLines.size)
        }

    val nullIndices: MutableList<Int> = mutableListOf()
    val intersections = lines.mapIndexed { i, line ->
        line.intersection(perpendicular) ?: run {
            nullIndices.add(i)
        }.let{ null }
    }.filterNotNull()

    // Filter out lines with no intersection
    val goodLines = if (nullIndices.isNotEmpty()) {
        lines.filterIndexed { i, _ -> !nullIndices.contains(i) }
    } else lines

    // Now run DB scan and throw away all lines except those that have the first point in each cluster
    return DBScan(intersections).map {
        goodLines[it.first()]
    }
}

fun findCorners(src: Mat): Pair<List<Line>, List<Line>>? {
    val tmpMat = Mat()
    resize(src, tmpMat)
    cvtColor(tmpMat, tmpMat, COLOR_BGR2GRAY)
    detectEdges(tmpMat, tmpMat)
    val allLines = detectLines(tmpMat)

    if (allLines.size > 400){
        return null
    }

//    return cluster(allLines)

    val agglomerative = AverageAgglomerative(allLines, numClusters = 2)
    agglomerative.run()

    var (verticals, horizontals) = try {
        agglomerative.clusters
            .map { cluster -> cluster.value.map { allLines[it] } }
            .let{ Pair(it[0], it[1]) }
    } catch (e: IndexOutOfBoundsException){
        return null
    }

    verticals = eliminateSimilarLines(verticals, horizontals)
    horizontals = eliminateSimilarLines(horizontals, verticals)
    return Pair(verticals, horizontals)

}