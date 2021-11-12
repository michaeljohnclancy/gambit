package com.wadiyatalkinabeet.gambit.domain.math.datastructures

import kotlin.math.*

typealias Vector = Point

class Point(val x: Float, val y: Float): Comparable<Point> {

    val length: Float by lazy { sqrt(x.pow(2) + y.pow(2)) }
    val normalized: Point by lazy { this / this.length }

    override fun compareTo(other: Point): Int {
        if (x == other.x) return y.compareTo(other.y)
        return x.compareTo(other.x)
    }

    override fun toString(): String {
        return "($x, $y)"
    }

    operator fun plus(point2: Point): Point {
        return Point(this.x + point2.x, this.y + point2.y)
    }

    operator fun unaryMinus(): Point {
        return Point(-this.x, -this.y)
    }

    operator fun minus(point2: Point): Point {
        return Point(this.x - point2.x, this.y - point2.y)
    }

    operator fun times(scale: Float): Point {
        return Point(x * scale, y * scale)
    }

    operator fun div(scale: Float): Point {
        return Point(x / scale, y / scale)
    }

    fun dot(point2: Point): Float {
        return this.x * point2.x + this.y * point2.y
    }

    fun cross(origin: Point, p2: Point): Float {
        return (p2.x - origin.x) * (this.y - origin.y) - (p2.y - origin.y) * (this.x - origin.x)
    }

    fun isLeftOfLine(from: Point, to: Point): Boolean {
        return cross(from, to) > 0
    }

    fun distanceToLine(a: Point, b: Point): Float {
        return abs((b.x - a.x) * (a.y - this.y) - (a.x - this.x) * (b.y - a.y)) /
                sqrt((b.x - a.x).pow(2) + (b.y - a.y).pow(2))
    }

    fun euclideanDistanceTo(that: Point): Float {
        return EUCLIDEAN_DISTANCE_FUNC(this, that)
    }

    fun manhattanDistanceTo(that: Point): Float {
        return MANHATTAN_DISTANCE_FUNC(this, that)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Point

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    companion object {
        // < 0 : Counterclockwise
        // = 0 : p, q and r are colinear
        // > 0 : Clockwise
        fun orientation(p: Point, q: Point, r: Point): Float {
            return (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y)
        }

        val EUCLIDEAN_DISTANCE_FUNC: (Point, Point) -> (Float) = { p, q ->
            val dx = p.x - q.x
            val dy = p.y - q.y
            sqrt((dx * dx + dy * dy))
        }

        val MANHATTAN_DISTANCE_FUNC: (Point, Point) -> (Float) = { p, q ->
            val dx = p.x - q.x
            val dy = p.y - q.y
            sqrt((dx * dx + dy * dy))
        }
    }
}

data class Segment(val p0: Point, val p1: Point) {
    constructor(x0: Float, y0: Float, x1: Float, y1: Float) : this(Point(x0,y0), Point(x1, y1))

    val length: Float by lazy { p0.euclideanDistanceTo(p1) }

    operator fun times(scale: Float): Segment {
        return Segment(
            this.p0.x * scale,
            this.p0.y * scale,
            this.p1.x * scale,
            this.p1.y * scale
        )
    }

    fun angleTo(line: Segment): Float {
        val v1 = this.p1 - this.p0
        val v2 = line.p1 - line.p0
        return acos((v1.dot(v2)) / (v1.length * v2.length) )
    }
}

data class Line(val rho: Float, val theta: Float) {
    companion object {}

    // Minimum angle between two lines
    fun angleTo(line: Line): Float {
        val delta = abs(this.theta - line.theta)
        return min(delta, (PI.toFloat() - delta))
    }

    fun intersection(line: Line): Point? {
        val cos0 = cos(this.theta)
        val cos1 = cos(line.theta)
        val sin0 = sin(this.theta)
        val sin1 = sin(line.theta)
        if (cos1 * sin0 - cos0 * sin1 == 0f || sin1 * cos0 - sin0 * cos1 == 0f)
            return null
        return Point(
            (sin0 * line.rho - sin1 * this.rho) / (cos1 * sin0 - cos0 * sin1),
            (cos0 * line.rho - cos1 * this.rho) / (sin1 * cos0 - sin0 * cos1)
        )
    }

    // This is only a quick translation for drawing to screen
    fun toSegment(len: Float = 1000.0f): Segment {
        val toLine = Vector(cos(theta), sin(theta)) * rho
        val normal = Vector(-sin(theta), cos(theta))
        return Segment(
            toLine - normal * len,
            toLine + normal * len
        )
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
    val maxError = 0.0625 * (length + segment2.length)

    return (length / ds > maxError).and(segment2.length / ds > maxError)
}

fun Segment.toPoints(nPoints: Int): List<Point> {
    return IntRange(0, nPoints - 1).map {
        Point(
            x = p0.x - (p1.x - p0.x) * (it * (1 / nPoints)),
            y = p0.y - (p1.y - p0.y) * (it * (1 / nPoints))
        )
    }
}