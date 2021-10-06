package com.wadiyatalkinabeet.gambit.math.algorithms

import com.wadiyatalkinabeet.gambit.math.cartesianToHomogenous
import com.wadiyatalkinabeet.gambit.math.homogenousToCartesian
import com.wadiyatalkinabeet.gambit.math.ones
import org.jetbrains.kotlinx.multik.api.empty
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.junit.Assert
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import org.opencv.core.Mat.ones

internal class RANSACTest {

    @Test
    fun cartesianToHomogenousTest() {
        val cartesianCoordinates = mk.ndarray(mk[mk[0,1], mk[2,3], mk[4,5]])
        val expectedHomogenousCoordinates = mk.ndarray(mk[mk[0,1,1], mk[2,3,1], mk[4,5,1]])

        val actualHomogenousCoordinates = cartesianToHomogenous(cartesianCoordinates)
        Assert.assertEquals(actualHomogenousCoordinates.asDNArray(), expectedHomogenousCoordinates.asDNArray())
    }

    @Test
    fun homogenousToCartesianTest(){
        val homogenousCoordinates = mk.ndarray(mk[mk[0,1,1], mk[2,3,1], mk[4,5,1]])
        val expectedCartesianCoordinates = mk.ndarray(mk[mk[0,1], mk[2,3], mk[4,5]])

        val actualCartesianCoordinates = homogenousToCartesian(homogenousCoordinates)
        Assert.assertEquals(actualCartesianCoordinates.asDNArray(), expectedCartesianCoordinates.asDNArray())

    }

    @Test
    fun distinctIndexed() {

    }
}