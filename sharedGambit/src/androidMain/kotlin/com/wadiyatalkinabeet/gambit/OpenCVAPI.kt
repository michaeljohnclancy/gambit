package com.wadiyatalkinabeet.gambit

import org.opencv.imgproc.Imgproc

actual typealias Mat = org.opencv.core.Mat

actual fun canny(
    matIn: Mat,
    matOut: Mat,
    lowerThreshold: Double,
    upperThreshold: Double,
    apertureSize: Int
) {
    Imgproc.Canny(matIn, matOut, lowerThreshold, upperThreshold, apertureSize)

}


