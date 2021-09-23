package com.wadiyatalkinabeet.gambit.math.geometry.algorithms

import com.wadiyatalkinabeet.gambit.math.datastructures.Point

private fun convexHull(points: Array<Point>): Collection<Point> {
    if (points.size < 3) throw IllegalArgumentException("there must be at least 3 points")
    val left = points.minOrNull()!!
    val right = points.maxOrNull()!!
    return quickHull(points.asList(), left, right) + quickHull(points.asList(), right, left)
}

fun quickHull(points: Collection<Point>, first: Point, second: Point): Collection<Point> {
    val pointsLeftOfLine = points
        .filter { it.isLeftOfLine(first, second) }
        .map { Pair(it, it.distanceToLine(first, second)) }
    return if (pointsLeftOfLine.isEmpty()) {
        listOf(second)
    } else {
        val max = pointsLeftOfLine.maxByOrNull { it.second }!!.first
        val newPoints = pointsLeftOfLine.map { it.first }
        quickHull(newPoints, first, max) + quickHull(newPoints, max, second)
    }
}
