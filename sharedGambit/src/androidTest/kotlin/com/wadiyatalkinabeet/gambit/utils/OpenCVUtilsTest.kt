package com.wadiyatalkinabeet.gambit.utils

import com.wadiyatalkinabeet.gambit.isSimilarTo
import com.wadiyatalkinabeet.gambit.toPoints
import ru.ifmo.ctddev.igushkin.cg.geometry.Point

import kotlin.test.Test
import ru.ifmo.ctddev.igushkin.cg.geometry.Segment
import kotlin.test.Ignore

internal class OpenCVUtilsTest {


    @Test
    fun isSimilarTo() {
        val segment1 = Segment(50.0, 130.0, 90.0, 130.0)
        val segment2 = Segment(100.0, 130.0, 120.0, 132.0)
        val segment3 = Segment(50.0, 130.0, 5.0, 20.0)
        assert(segment1.isSimilarTo(segment2))
        assert(!segment1.isSimilarTo(segment3))
    }

    @Test
    fun ifPointsGeneratedFromSegment_thenPointsLieWithinTheSegment() {
        val segment = Segment(50.0, 130.0, 90.0, 130.0)
        val pointsOnLine = segment.toPoints(10)
        pointsOnLine.forEach { assert( isPointOnSegment(it, segment) ) }
    }

    @Ignore("How to generate a mat that we know the median of?")
    @Test
    fun medianMat() {
    }
}

fun isPointOnSegment(point: Point, segment: Segment): Boolean{
    return segment.x0 <= point.x && point.x <= segment.x1
            && segment.y0 <= point.y && point.y <= segment.y1
}