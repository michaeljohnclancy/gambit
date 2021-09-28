package com.wadiyatalkinabeet.gambit.math.geometry

import com.wadiyatalkinabeet.gambit.math.datastructures.Line
import com.wadiyatalkinabeet.gambit.math.datastructures.Point
import com.wadiyatalkinabeet.gambit.math.datastructures.Segment
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.lang.Math.abs
import java.lang.Math.sqrt
import kotlin.math.PI
import kotlin.math.pow
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BasicGeomTest {
    companion object {
        // Permissible magnitude of floating point errors
        private const val EPSILON = 1e-5

        // Check equality, within floating point error
        fun assertApproxEqual(a: Float, b: Float) {
            assertTrue(abs(a - b) <= EPSILON)
        }

        fun assertApproxEqual(a: Point, b: Point) {
            assertTrue(abs(a.x - b.x) <= EPSILON)
            assertTrue(abs(a.y - b.y) <= EPSILON)
        }

        fun assertApproxEqual(a: Segment, b: Segment) {
            assertTrue(abs(a.p0.x - b.p0.x) <= EPSILON)
            assertTrue(abs(a.p0.y - b.p0.y) <= EPSILON)
            assertTrue(abs(a.p1.x - b.p1.x) <= EPSILON)
            assertTrue(abs(a.p1.y - b.p1.y) <= EPSILON)
        }
    }

    @Nested
    class PointTest {
        companion object {
            // Some example points for testing
            private val P0 = Point(0f, 0f)
            private val P1 = Point(1f, 0f)
            private val P2 = Point(0.5f, 0.5f)
            private val P3 = Point(0f, -2f)

            // Some example lines for testing
            private val L_VERTICAL = Segment(
                Point(0f, -1f), Point(0f, 1f)
            )
            private val L_HORIZONTAL = Segment(
                Point(-1f, 0f), Point(1f, 0f)
            )
            private val L_DIAGONAL = Segment(
                Point(-1f, -1f), Point(1f, 1f)
            )
        }

        @Test
        fun vectorLength() {
            assertApproxEqual(P0.length, 0f)
            assertApproxEqual(P1.length, 1f)
            assertApproxEqual(P2.length, sqrt(0.5).toFloat())
            assertApproxEqual(P3.length, 2f)
        }

        @Test
        fun vectorAddition() {
            assertApproxEqual(P0 + P0, P0)
            assertApproxEqual(P0 + P3, P3)
            assertApproxEqual(P1 + P2, Point(1.5f, 0.5f))
            assertApproxEqual(P3 + P3, Point(0f, -4f))
        }

        @Test
        fun vectorSubtraction() {
            assertApproxEqual(P0 - P0, P0)
            assertApproxEqual(P3 - P3, P0)
            assertApproxEqual(P0 - P3, Point(0f, 2f))
            assertApproxEqual(P1 - P2, Point(0.5f, -0.5f))
        }

        @Test
        fun vectorScaling() {
            assertApproxEqual(P0 * 10f, P0)
            assertApproxEqual(P1 * 2f, Point(2f, 0f))
            assertApproxEqual(P2 * -2f, Point(-1f, -1f))
            assertApproxEqual(P3 * 2f, Point(0f, -4f))
        }

        @Test
        fun vectorDotProduct() {
            assertApproxEqual(P0.dot(P0), 0f)
            assertApproxEqual(P1.dot(P1), 1f)
            assertApproxEqual(P1.dot(P2), 0.5f)
            assertApproxEqual(P2.dot(P2), P2.length.pow(2))
        }

        @Test
        fun vectorCrossProduct() {
            assertApproxEqual(P1.cross(P0, P1), 0f)
            assertApproxEqual(
                P2.cross(P0, P1),
                // Area of the parallelogram
                P2.y * P1.x
            )
            //TODO Tests where 'origin' is non-zero
        }

        @Test
        fun isLeftOfLine() {
            assertTrue(
                Point(-0.01f, 0f)
                    .isLeftOfLine(L_VERTICAL.p0, L_VERTICAL.p1)
            )
            assertFalse(
                Point(0.01f, 0f)
                    .isLeftOfLine(L_VERTICAL.p0, L_VERTICAL.p1)
            )
            assertTrue(
                Point(0f, 0.01f)
                    .isLeftOfLine(L_DIAGONAL.p0, L_DIAGONAL.p1)
            )
            assertFalse(
                Point(0f, -0.01f)
                    .isLeftOfLine(L_DIAGONAL.p0, L_DIAGONAL.p1)
            )
        }

        @Test
        fun distanceToLine() {
            assertApproxEqual(
                Point(-1f, 10f).distanceToLine(L_VERTICAL.p0, L_VERTICAL.p1),
                1f
            )
            assertApproxEqual(
                Point(1f, 100f).distanceToLine(L_VERTICAL.p0, L_VERTICAL.p1),
                1f
            )
            assertApproxEqual(
                Point(50f, 2f).distanceToLine(L_HORIZONTAL.p0, L_HORIZONTAL.p1),
                2f
            )
            assertApproxEqual(
                Point(1f, -1f).distanceToLine(L_DIAGONAL.p0, L_DIAGONAL.p1),
                sqrt(2.0).toFloat()
            )
        }

        @Test
        fun euclidianDistanceTo() {
            assertApproxEqual(P0.euclideanDistanceTo(P1), 1f)
            assertApproxEqual(P1.euclideanDistanceTo(P2), (P2 - P1).length)
            assertApproxEqual(P2.euclideanDistanceTo(P3), (P3 - P2).length)
        }

        //TODO Manhattan distance
    }

    @Nested
    class SegmentTest {
        companion object {
            // Some example segments for testing
            private val SEG0 = Segment(Point(0f, 0f), Point(0f, 0f))
            private val SEG1 = Segment(Point(-1f, 0f), Point(1f, 0f))
            private val SEG2 = Segment(Point(-1f, -1f), Point(1f, 1f))
            private val SEG3 = Segment(Point(-1f, 1f), Point(1f, -1f))
        }

        @Test
        fun segmentLength() {
            assertApproxEqual(SEG0.length, 0f)
            assertApproxEqual(SEG1.length, 2f)
            assertApproxEqual(SEG2.length, sqrt(8.0).toFloat())
        }

        @Test
        fun segmentScaling() {
            assertApproxEqual(SEG0 * 2f, SEG0)
            assertApproxEqual(
                SEG1 * 2f,
                Segment(Point(-2f, 0f), Point(2f, 0f))
            )
        }

        @Test
        fun angleTo() {
            assertApproxEqual(SEG1.angleTo(SEG1), 0f)
            assertApproxEqual(SEG1.angleTo(SEG2), PI.toFloat() / 4)
            assertApproxEqual(SEG2.angleTo(SEG3), PI.toFloat() / 2)
        }
    }

    @Nested
    class LineTest {
        companion object {
            // The two diagonals of a 100x100 box
            private val DIAGONAL_ONE = Line(0f, 3 * PI.toFloat() / 4)
            private val DIAGONAL_TWO = Line(sqrt(5000.0).toFloat(),PI.toFloat() / 4)
        }

        @Test
        fun angleTo() {
            assertApproxEqual(
                DIAGONAL_ONE.angleTo(DIAGONAL_ONE),
                0f
            )
            assertApproxEqual(
                DIAGONAL_ONE.angleTo(DIAGONAL_TWO),
                PI.toFloat() / 2
            )
        }

        @Test
        fun intersection() {
            assertEquals(null, DIAGONAL_ONE.intersection(DIAGONAL_ONE))
            assertApproxEqual(
                DIAGONAL_ONE.intersection(DIAGONAL_TWO)!!,
                Point(50f, 50f)
            )
        }
    }
}