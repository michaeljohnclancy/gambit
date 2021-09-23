package com.wadiyatalkinabeet.gambit

import com.wadiyatalkinabeet.gambit.cv.Mat
import com.wadiyatalkinabeet.gambit.math.geometry.Point
import com.wadiyatalkinabeet.gambit.math.geometry.Segment


internal interface StraightLineDetector {
    fun getLines(mat: Mat): List<Segment>
}

internal interface LatticePointDetector {
    fun getLatticePoints(mat: Mat): List<Point>
}

internal interface CornerDetector : StraightLineDetector, LatticePointDetector {
    fun getCornerPoints(mat: Mat): List<Point>
}