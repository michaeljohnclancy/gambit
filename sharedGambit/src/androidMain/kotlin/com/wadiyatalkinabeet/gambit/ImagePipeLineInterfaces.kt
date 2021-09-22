package com.wadiyatalkinabeet.gambit

import org.opencv.core.Mat
import ru.ifmo.ctddev.igushkin.cg.geometry.Point
import ru.ifmo.ctddev.igushkin.cg.geometry.Segment


internal interface StraightLineDetector {
    fun getLines(mat: Mat): List<Segment>
}

internal interface LatticePointDetector {
    fun getLatticePoints(mat: Mat): List<Point>
}

internal interface CornerDetector : StraightLineDetector, LatticePointDetector {
    fun getCornerPoints(mat: Mat): List<Point>
}