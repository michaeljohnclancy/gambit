package com.wadiyatalkinabeet.gambit.domain.cv

import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Line
import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Point

data class ImageAnalysisState(
    val sourceMat: Mat,
    val resizeWidth: Double = 1200.0,
    var horizontalLines: List<Line>? = null,
    var verticalLines: List<Line>? = null,
    var cornerPoints: List<Point>? = null,
    var warpedTransformedMat: Mat? = null,
) {
    val scale = resizeWidth / sourceMat.width().toDouble()
}

class NotEnoughFeaturesException(message: String) : Exception(message)

