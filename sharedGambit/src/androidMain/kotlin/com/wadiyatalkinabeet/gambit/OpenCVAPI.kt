package com.wadiyatalkinabeet.gambit

import org.opencv.calib3d.Calib3d
import org.opencv.imgproc.Imgproc

actual typealias Mat = org.opencv.core.Mat
actual typealias MatOfPoint2f = org.opencv.core.MatOfPoint2f
actual typealias Size = org.opencv.core.Size

actual fun canny(
    src: Mat,
    dst: Mat,
    lowerThreshold: Double,
    upperThreshold: Double,
    apertureSize: Int
) = Imgproc.Canny(src, dst, lowerThreshold, upperThreshold, apertureSize)

actual fun cvtColor(
    src: Mat,
    dst: Mat,
    colorOut: Int
) = Imgproc.cvtColor(src, dst, colorOut)

actual fun houghLines(
    src: Mat,
    lines: Mat,
    rho: Double,
    theta: Double,
    threshold: Int,
    srn: Double
) =  Imgproc.HoughLines(src, lines, rho, theta, threshold, srn)

actual fun findHomography(
    srcPoints: MatOfPoint2f,
    dstPoints: MatOfPoint2f
) = Calib3d.findHomography(srcPoints, dstPoints)

actual fun warpPerspective(
    src: Mat,
    dst: Mat,
    transformationMatrix: Mat,
    dsize: Size
) = Imgproc.warpPerspective(src, dst, transformationMatrix, dsize)

actual fun sobel(
    src: Mat,
    dst: Mat,
    ddepth: Int,
    dx: Int,
    dy: Int
) = Imgproc.Sobel(src, dst, ddepth, dx, dy)

actual fun resize(
    src: Mat,
    dst: Mat,
    dsize: Size
) = Imgproc.resize(src, dst, dsize)