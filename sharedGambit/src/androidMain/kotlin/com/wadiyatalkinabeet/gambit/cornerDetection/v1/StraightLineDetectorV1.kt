package com.wadiyatalkinabeet.gambit.cornerDetection.v1

import com.wadiyatalkinabeet.gambit.*
import com.wadiyatalkinabeet.gambit.utils.DisjointSet
import org.opencv.core.*
import org.opencv.core.CvType.*
import org.opencv.imgproc.Imgproc.*

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Size
import ru.ifmo.ctddev.igushkin.cg.geometry.Point
import ru.ifmo.ctddev.igushkin.cg.geometry.Segment
import kotlin.collections.ArrayList
import kotlin.math.*
import org.opencv.imgproc.Imgproc.cvtColor

const val EPSILON = 1e-5

typealias CLAHEParameters = Triple<Double, Size, Int>

internal val claheParameters: List<CLAHEParameters> = listOf(
    Triple(3.0, Size(2.0, 6.0), 5),
    Triple(3.0, Size(6.0, 2.0), 5),
    Triple(5.0, Size(3.0, 3.0), 5),
    Triple(0.0, Size(0.0, 0.0), 0)
)

internal fun applyCLAHE(
    mat: Mat,
    limit: Double = 2.0,
    gridSize: Size = Size(3.0, 3.0),
    nIterations: Int = 5
): Mat {

    val claheMat = Mat()
    cvtColor(mat, claheMat, COLOR_BGR2GRAY)

    val clahe = createCLAHE(limit, gridSize)
    for (i in 0 until nIterations) {
        clahe.apply(claheMat, claheMat)
    }
    if (limit != 0.0) {
        val kernel = Mat.ones(Size(10.0, 10.0), CV_8U)
        morphologyEx(claheMat, claheMat, MORPH_CLOSE, kernel)

    }

    return claheMat
}

internal fun applyAutoCanny(
    mat: Mat,
    sigma: Double = 0.25
): Mat {
    val median = mat.median()
    medianBlur(mat, mat, 5)
    GaussianBlur(mat, mat, Size(7.0, 7.0), 2.0)
    val lowerThreshold: Double = max(0.0, (1.0 - sigma) * median)
    val upperThreshold: Double = min(255.0, (1.0 + sigma) * median)
    Canny(mat, mat, lowerThreshold, upperThreshold)
    return mat
}

internal fun applyHoughLinesP(
    mat: Mat,
    beta: Double = 2.0
): List<Segment> {
    val lines = Mat()

    HoughLinesP(
        mat, lines, 1.0, PI / 360.0 * beta,
        40, 50.0, 15.0
    )

    val segments: ArrayList<Segment> = arrayListOf()

    for (i in 0 until lines.rows()) {
        val line = lines.get(i, 0)
        segments.add(
            Segment(x0 = line[0], y0 = line[1], x1 = line[2], y1 = line[3])
        )
    }

    return segments
}

internal fun clusterSegments(segments: List<Segment>): List<List<Segment>> {

    val (preGroupIndices1, preGroupIndices2) = segments.indices.partition {
        abs(segments[it].x0 - segments[it].x1) < abs(segments[it].y0 - segments[it].y1)
    }

    val disjointSet = DisjointSet(size = segments.size)

    disjointSet.populateDisjointSet(segments, preGroupIndices1, Segment::isSimilarTo)
    disjointSet.populateDisjointSet(
        segments,
        preGroupIndices2,
        Segment::isSimilarTo,
    )
    return IntRange(0, segments.size - 1)
        .groupBy { disjointSet.find(it) }
        .values.map { it.map(segments::get) }
}

internal fun mergeSegments(segments: List<Segment>, scale: Double = 4.0): Segment {

    return MatOfPoint2f(
        *segments
            .flatMap { it.toPoints(nPoints = 10) }
            .map(Point::toOpenCV)
            .toTypedArray()
    ).let {
        val centre = Point()
        val radius = FloatArray(1)
        val lineMat = Mat()

        minEnclosingCircle(it, centre, radius)
        fitLine(it, lineMat, DIST_L2, 0.0, 0.01, 0.01)

        val w = radius[0] * PI / 2.0
        val vx = lineMat.get(0, 0)[0]
        val vy = lineMat.get(1, 0)[0]
        val cx = lineMat.get(2, 0)[0]
        val cy = lineMat.get(3, 0)[0]

        val segment = Segment(cx - (vx * w), cy - (vy * w), cx + (vx * w), cy + (vy * w))

        Segment(
            x0 = scale(segment.x0, segment.x1, scale),
            y0 = scale(segment.y0, segment.y1, scale),
            x1 = scale(segment.x1, segment.x0, scale),
            y1 = scale(segment.y1, segment.y0, scale)
        )
    }
}

private fun scale(x: Double, y: Double, scale: Double): Double {
    return x * (1 + scale) / 2 + y * (1 - scale) / 2
}


