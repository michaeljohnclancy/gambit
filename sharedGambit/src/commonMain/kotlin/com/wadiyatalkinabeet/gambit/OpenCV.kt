package com.wadiyatalkinabeet.gambit

//expect class Image

//expect fun ByteArray.toNativeImage(): Image?

//expect fun randomUUID(): String
//expect fun randomUUID(): String

expect class Mat

expect fun loadChessboardExampleImage(): Mat

expect fun canny(matIn: Mat, matOut: Mat, lowerThreshold: Double, upperThreshold: Double, apertureSize: Int)

fun processImage(matIn: Mat, matOut: Mat, lowerThreshold: Double, upperThreshold: Double, apertureSize: Int): Mat {
    canny(matIn, matOut, lowerThreshold, upperThreshold, apertureSize)
    return matOut

}