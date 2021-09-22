package com.wadiyatalkinabeet.gambit

//expect class Image

//expect fun ByteArray.toNativeImage(): Image?

//expect fun randomUUID(): String
//expect fun randomUUID(): String

expect class Mat
expect class MatOfPoint2f
expect class Size

expect fun loadChessboardExampleImage(): Mat

expect fun cvtColor(src: Mat, dst: Mat, colorOut: Int)

expect fun resize(src: Mat, dst: Mat, dsize: Size)

expect fun canny(src: Mat, dst: Mat, lowerThreshold: Double, upperThreshold: Double, apertureSize: Int)

expect fun houghLines(src: Mat, lines: Mat, rho: Double, theta: Double, threshold: Int, srn: Double)

expect fun findHomography(srcPoints: MatOfPoint2f, dstPoints: MatOfPoint2f): Mat

expect fun warpPerspective(src: Mat, dst: Mat, transformationMatrix: Mat, dsize: Size)

expect fun sobel(src: Mat, dst: Mat, ddepth: Int, dx: Int, dy: Int)

fun processImage(matIn: Mat, matOut: Mat, lowerThreshold: Double, upperThreshold: Double, apertureSize: Int): Mat {
    canny(matIn, matOut, lowerThreshold, upperThreshold, apertureSize)
    return matOut

}