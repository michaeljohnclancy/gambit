package com.wadiyatalkinabeet.gambit.math.geometry

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

typealias Vector = Point

class Point(val x: Double, val y: Double): Comparable<Point> {
    override fun compareTo(other: Point): Int {
        if (x == other.x) return y.compareTo(other.y)
        return x.compareTo(other.x)
    }

    fun length() = sqrt(x.pow(2) + y.pow(2))

    fun isLeftOfLine(from: Point, to: Point): Boolean {
        return cross(from, to) > 0
    }

    operator fun minus(point2: Point): Point {
        return Point(this.x - point2.x, this.y - point2.y)
    }

    fun dot(point2: Point): Double {
        return this.x * point2.x + this.y * point2.y
    }

    fun cross(origin: Point, p2: Point): Double {
        return (p2.x - origin.x) * (this.y - origin.y) - (p2.y - origin.y) * (this.x - origin.x)
    }

    fun distanceToLine(a: Point, b: Point): Double {
        return abs((b.x - a.x) * (a.y - this.y) - (a.x - this.x) * (b.y - a.y)) /
                sqrt((b.x - a.x).pow(2) + (b.y - a.y).pow(2))
    }

    fun euclideanDistanceTo(that: Point): Double {
        return EUCLIDEAN_DISTANCE_FUNC(this, that)
    }

    fun manhattanDistanceTo(that: Point): Double {
        return MANHATTAN_DISTANCE_FUNC(this, that)
    }

    companion object {
        // < 0 : Counterclockwise
        // = 0 : p, q and r are colinear
        // > 0 : Clockwise
        fun orientation(p: Point, q: Point, r: Point): Double {
            return (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y)
        }

        val EUCLIDEAN_DISTANCE_FUNC: (Point, Point) -> (Double) = { p, q ->
            val dx = p.x - q.x
            val dy = p.y - q.y
            sqrt((dx * dx + dy * dy).toDouble())
        }

        val MANHATTAN_DISTANCE_FUNC: (Point, Point) -> (Double) = { p, q ->
            val dx = p.x - q.x
            val dy = p.y - q.y
            sqrt((dx * dx + dy * dy).toDouble())
        }
    }
}

data class Segment(val p0: Point, val p1: Point) {
    constructor(x0: Double, y0: Double, x1: Double, y1: Double) : this(Point(x0,y0), Point(x1, y1))

    fun length() = p0.euclideanDistanceTo(p1)

    operator fun times(scale: Double): Segment {
        return Segment(this.p0.x * scale, this.p0.y * scale, this.p1.x * scale, this.p1.y * scale)
    }
    operator fun times(scale: Float): Segment {
        return Segment(
            (this.p0.x * scale),
            (this.p0.y * scale),
            (this.p1.x * scale),
            (this.p1.y * scale)
        )
    }

    fun angleTo(line: Segment): Double {
        val v1 = this.p1 - this.p0
        val v2 = line.p1 - line.p0
        return acos((v1.dot(v2)) / (v1.length() * v2.length()) )
    }
}


//Move below out of here

fun Segment.isSimilarTo(segment2: Segment): Boolean {
    val d1x = segment2.p0.distanceToLine(p0, p1)
    val d2x = segment2.p1.distanceToLine(p0, p1)
    val d1y = p0.distanceToLine(segment2.p0, segment2.p1)
    val d2y = p1.distanceToLine(segment2.p0, segment2.p1)

    //FIXME: Why EPSILON?
    //    val ds = (d1x + d2x + d1y + d2y) / 4 + EPSILON
    val ds = (d1x + d2x + d1y + d2y) / 4
    val maxError = 0.0625 * (length() + segment2.length())

    return (length() / ds > maxError).and(segment2.length() / ds > maxError)
}

fun Segment.toPoints(nPoints: Int): List<Point> {
    return IntRange(0, nPoints - 1).map {
        Point(
            x = p0.x - (p1.x - p0.x) * (it * (1 / nPoints)),
            y = p0.y - (p1.y - p0.y) * (it * (1 / nPoints))
        )
    }
}