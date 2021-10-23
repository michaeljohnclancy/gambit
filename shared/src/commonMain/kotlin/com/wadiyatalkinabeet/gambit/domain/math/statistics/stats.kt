package com.wadiyatalkinabeet.gambit.math.statistics

fun median(m: DoubleArray): Double {
    val middle = m.size / 2
    return if (m.size % 2 == 1) {
        m[middle]
    } else {
        (m[middle - 1] + m[middle]) / 2.0
    }
}