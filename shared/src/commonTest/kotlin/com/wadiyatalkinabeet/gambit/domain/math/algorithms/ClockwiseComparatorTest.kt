package com.wadiyatalkinabeet.gambit.domain.math.algorithms

import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Point
import kotlin.test.Test
import kotlin.test.assertContentEquals

internal class ClockwiseComparatorTest {

    @Test
    fun sortClockwise() {
        val expectedOrderPoints = listOf(
            Point(50f, 50f),
            Point(50f, 0f),
            Point(0f, 0f),
            Point(0f, 50f)
        )

        val unorderedPoints = listOf(
            Point(0f, 50f),
            Point(0f, 0f),
            Point(50f, 50f),
            Point(50f, 0f)
        )

        assertContentEquals(expectedOrderPoints, unorderedPoints.sortClockwise())
    }
}