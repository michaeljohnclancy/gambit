package com.wadiyatalkinabeet.gambit

import com.wadiyatalkinabeet.gambit.math.geometry.Point
import com.wadiyatalkinabeet.gambit.math.geometry.Segment
import com.wadiyatalkinabeet.gambit.math.geometry.isSimilarTo
import com.wadiyatalkinabeet.gambit.math.geometry.toPoints

import kotlin.test.Test
import kotlin.test.Ignore
import kotlin.test.assertTrue

internal class OpenCVUtilsTest {

    @Test
    fun isSimilarTo() {
        val segment1 = Segment(50.0, 130.0, 90.0, 130.0)
        val segment2 = Segment(100.0, 130.0, 120.0, 132.0)
        val segment3 = Segment(50.0, 130.0, 5.0, 20.0)
        assertTrue(segment1.isSimilarTo(segment2))
        assertTrue(!segment1.isSimilarTo(segment3))
    }

    @Test
    fun ifPointsGeneratedFromSegment_thenPointsLieWithinTheSegment() {
        val segment = Segment(50.0, 130.0, 90.0, 130.0)
        val pointsOnLine = segment.toPoints(10)
        pointsOnLine.forEach { assertTrue( isPointOnSegment(it, segment) ) }
    }

    @Ignore()
    @Test
    fun medianMat() {
    }
}

fun isPointOnSegment(point: Point, segment: Segment): Boolean{
    return segment.p0.x <= point.x && point.x <= segment.p1.x
            && segment.p0.y <= point.y && point.y <= segment.p1.y
}