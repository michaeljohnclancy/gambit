package com.wadiyatalkinabeet.gambit.math.algorithms

import kotlin.test.Test
import kotlin.test.assertEquals

class MeshgridTest {

    @Test
    fun meshGridXY(){

        val in1 = floatArrayOf(0f, 0.5f, 1f)
        val in2 = floatArrayOf(0f, 1f)
        val expected1 = arrayOf(floatArrayOf(0f, 0.5f, 1f), floatArrayOf(0f, 0.5f, 1f))
        val expected2 = arrayOf(floatArrayOf(0f, 0f, 0f), floatArrayOf(1f, 1f, 1f))

        val (actual1, actual2) = meshGrid(in1, in2, MeshgridIndex.XY)

        assertEquals(actual1.size, expected1.size)
        assertEquals(actual2.size, expected2.size)

        actual1.indices.forEach{
            assert(expected1[it].contentEquals(actual1[it]))
        }
        actual2.indices.forEach{
            assert(expected2[it].contentEquals(actual2[it]))
        }

    }

    @Test
    fun meshGridIJ(){

        val in1 = floatArrayOf(0f, 0.5f, 1f)
        val in2 = floatArrayOf(0f, 1f)
        val expected1 = arrayOf(floatArrayOf(0f, 0f), floatArrayOf(0.5f, 0.5f), floatArrayOf(1f, 1f))
        val expected2 = arrayOf(floatArrayOf(0f, 1f), floatArrayOf(0f, 1f), floatArrayOf(0f, 1f))

        val (actual1, actual2) = meshGrid(in1, in2, MeshgridIndex.IJ)

        assertEquals(actual1.size, expected1.size)
        assertEquals(actual2.size, expected2.size)

        actual1.indices.forEach{
            assert(expected1[it].contentEquals(actual1[it]))
        }
        actual2.indices.forEach{
            assert(expected2[it].contentEquals(actual2[it]))
        }
    }
}