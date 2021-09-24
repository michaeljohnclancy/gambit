package com.wadiyatalkinabeet.gambit.cv

import com.wadiyatalkinabeet.gambit.cv.Mat
import com.wadiyatalkinabeet.gambit.math.datastructures.Point
import com.wadiyatalkinabeet.gambit.math.datastructures.Segment


internal interface StraightLineDetector {
    fun getLines(mat: Mat): List<Segment>
}

internal interface LatticePointDetector {
    fun getLatticePoints(mat: Mat): List<Point>
}

internal interface CornerDetector : StraightLineDetector, LatticePointDetector {
    fun getCornerPoints(mat: Mat): List<Point>
}