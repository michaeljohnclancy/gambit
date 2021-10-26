package com.wadiyatalkinabeet.gambit.math.geometry

import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Point
import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Segment
import com.wadiyatalkinabeet.gambit.domain.math.datastructures.isSimilarTo
import com.wadiyatalkinabeet.gambit.domain.math.datastructures.toPoints
import kotlin.test.Test
import kotlin.test.Ignore
import kotlin.test.assertTrue

internal class GeometryDataStructureTest {

    @Test
    fun isSimilarTo() {
        val segment1 = Segment(50.0f, 130.0f, 90.0f, 130.0f)
        val segment2 = Segment(100.0f, 130.0f, 120.0f, 132.0f)
        val segment3 = Segment(50.0f, 130.0f, 5.0f, 20.0f)
        assertTrue(segment1.isSimilarTo(segment2))
        assertTrue(!segment1.isSimilarTo(segment3))
    }

    @Test
    fun ifPointsGeneratedFromSegment_thenPointsLieWithinTheSegment() {
        val segment = Segment(50.0f, 130.0f, 90.0f, 130.0f)
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