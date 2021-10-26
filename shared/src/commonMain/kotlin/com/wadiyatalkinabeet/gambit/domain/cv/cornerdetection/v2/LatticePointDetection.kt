package com.wadiyatalkinabeet.gambit.domain.cv.cornerdetection.v2

import com.wadiyatalkinabeet.gambit.domain.cv.Point
import com.wadiyatalkinabeet.gambit.domain.math.algorithms.MeshgridIndex
import com.wadiyatalkinabeet.gambit.domain.math.algorithms.meshGrid
import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Line

import kotlin.math.cos
import kotlin.math.sin

fun findIntersectionPoints(
    lines: List<Line>, perpendicularLines: List<Line>
): Array<Array<Point?>> {

    val (rho1, rho2) = meshGrid(lines.map { it.rho }.toFloatArray(), perpendicularLines.map { it.rho }.toFloatArray(), MeshgridIndex.IJ)
    val (theta1, theta2) = meshGrid(lines.map { it.theta }.toFloatArray(), perpendicularLines.map { it.theta }.toFloatArray(), MeshgridIndex.IJ)

    val intersectionPoints = Array(rho1.size) {
        Array<Point?>(rho1[0].size) { null }
    }

    for (i in rho1.indices){
        for (j in rho1[i].indices){
            intersection(rho1 = rho1[i][j], theta1 = theta1[i][j], rho2 = rho2[i][j], theta2 = theta2[i][j])?.let {
                intersectionPoints[i][j] = it
            }
        }
    }
    return intersectionPoints
}

fun intersection(
    rho1: Float, theta1: Float, rho2: Float, theta2: Float
): Point? {
    val cos0 = cos(theta1)
    val cos1 = cos(theta2)
    val sin0 = sin(theta1)
    val sin1 = sin(theta2)
    return try {
        Point(
            (sin0 * rho2 - sin1 * rho1) / (cos1 * sin0 - cos0 * sin1),
            (cos0 * rho2 - cos1 * rho1) / (sin1 * cos0 - sin0 * cos1)
        )
    } catch (_: ArithmeticException) {
        null
    }
}
