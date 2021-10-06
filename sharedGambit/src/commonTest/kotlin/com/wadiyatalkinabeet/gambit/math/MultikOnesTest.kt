package com.wadiyatalkinabeet.gambit.math

import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.D3
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import kotlin.test.Test

class MultikOnesTest {

    @Test
    fun makeOnesNDArray(){
        val expected = mk.ndarray(mk[mk[1.0, 1.0, 1.0], mk[1.0, 1.0, 1.0]])
        val actual = mk.ones<Double, D2>(2, 3)
        assert(actual == expected)
    }

    @Test
    fun ifLengthOfArgsProvidedNotEqualToDimensionality_thenExceptionIsThrown(){
        assertThrows<IllegalArgumentException> {mk.ones<Double, D3>(2, 3)}
        assertThrows<IllegalArgumentException> {mk.ones<Double, D2>(2, 3, 5)}
    }
}