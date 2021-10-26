package com.wadiyatalkinabeet.gambit.domain.cv.cornerdetection.v2

import com.wadiyatalkinabeet.gambit.domain.cv.*
import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Line
import com.wadiyatalkinabeet.gambit.math.statistics.clustering.AverageAgglomerative
import com.wadiyatalkinabeet.gambit.math.statistics.clustering.DBScan
import com.wadiyatalkinabeet.gambit.math.statistics.clustering.FCluster
import kotlin.math.PI
import kotlin.math.abs

fun eliminateSimilarLines(lines: List<Line>, perpendicularLines: List<Line>): List<Line> {
    val perpendicular = perpendicularLines
        .fold(
            initial = Line(0.0f, 0.0f),
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

fun clusterLines(lines: List<Line>): Pair<List<Line>, List<Line>> {
    return AverageAgglomerative(lines, numClusters = 2).runClustering()
        .map { cluster -> cluster.value.map { lines[it] } }
        .let {
            val averageAngleToYAxisGroup0 = it[0].map { x -> x.angleTo(Line(0.0f,0.0f)) }.average()
            val averageAngleToYAxisGroup1 = it[1].map { x -> x.angleTo(Line(0.0f,0.0f)) }.average()
            if (averageAngleToYAxisGroup0 > averageAngleToYAxisGroup1)
            { Pair(it[0], it[1]) }
            else { Pair(it[1], it[0]) }
        }
}

fun detectLines(
    src: Mat,
    eliminateDiagonals: Boolean = false,
): List<Line> {

    val tmpMat = Mat()
    src.convertTo(src, CV_8UC1)
    canny(src, tmpMat, 90.0, 400.0, 3)
    houghLines(tmpMat, tmpMat, 1.0, PI /360, 100)

    var allLines = Line.fromHoughLines(tmpMat)
    // Looks like this would break diagonal views if enabled
    if (eliminateDiagonals) {
        fun diagonalFilter(line: Line) = ( abs(line.theta) < 0.524 || abs(line.theta - (PI / 2)) < 0.524
                )
        allLines = allLines.filter(::diagonalFilter)
    }
    return allLines
}

fun fCluster(lines: List<Line>, maxAngle: Double = PI /180): Pair<List<Line>, List<Line>>? {
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
