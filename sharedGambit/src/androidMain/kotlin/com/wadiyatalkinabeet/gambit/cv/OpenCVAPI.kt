package com.wadiyatalkinabeet.gambit.cv

import android.graphics.ImageFormat
import android.media.Image
import org.opencv.android.OpenCVLoader
import org.opencv.calib3d.Calib3d
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgcodecs.Imgcodecs.imread
import org.opencv.imgproc.Imgproc
import org.opencv.utils.Converters

actual fun initOpenCV() {
    OpenCVLoader.initDebug()
}

actual typealias Mat = org.opencv.core.Mat
actual typealias MatOfPoint2f = org.opencv.core.MatOfPoint2f
actual typealias MatOfPoint3f = org.opencv.core.MatOfPoint3f
actual typealias Point = org.opencv.core.Point
actual typealias Point3 = org.opencv.core.Point3
actual typealias Size = org.opencv.core.Size

actual fun multiply(src1: Mat, src2: Mat, dst: Mat) = Core.multiply(src1, src2, dst)

actual fun gemm(src1: Mat, src2: Mat, alpha: Double, src3: Mat, beta: Double, dst: Mat) = Core.gemm(src1, src2, alpha, src3, beta, dst)

actual fun vector_Point2d_to_Mat(points: List<Point>): Mat = Converters.vector_Point2d_to_Mat(points)

actual fun canny(
    src: Mat,
    dst: Mat,
    lowerThreshold: Double,
    upperThreshold: Double,
) =  Imgproc.Canny(src, dst, lowerThreshold, upperThreshold)

actual fun canny(
    src: Mat,
    dst: Mat,
    lowerThreshold: Double,
    upperThreshold: Double,
    apertureSize: Int
) = Imgproc.Canny(src, dst, lowerThreshold, upperThreshold)

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
) =  Imgproc.HoughLines(src, lines, rho, theta, threshold)

actual fun houghLinesP(
    src: Mat, lines: Mat,
    rho: Double, theta: Double,
    threshold: Int, minLineLength: Double,
    maxLineGap: Double
) = Imgproc.HoughLinesP(src, lines, rho, theta, threshold, minLineLength, maxLineGap)

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
    dy: Int,
    kernelSize: Int
) = Imgproc.Sobel(src, dst, ddepth, dx, dy, kernelSize)

actual fun medianBlur(
    src: Mat,
    dst: Mat,
    kernelSize: Int
) = Imgproc.medianBlur(src, dst, kernelSize)

actual fun gaussianBlur(
    src: Mat, dst: Mat,
    kernelSize: Size, sigmaX: Double
) = Imgproc.GaussianBlur(src, dst, kernelSize, sigmaX)

actual fun resize(
    src: Mat,
    dst: Mat,
    dsize: Size
) = Imgproc.resize(src, dst, dsize)

actual fun Mat.reshape(
    cn: Int, rows: Int
): Mat = reshape(cn, rows)

actual operator fun Mat.get(
    row: Int, column: Int,
): DoubleArray? = get(row, column)

actual fun Mat.width() = width()

actual fun Mat.height() = height()

actual fun Mat.size(): Size = size()

actual fun loadChessboardExampleImage(): Mat {
    return imread("src/commonTest/res/example_chessboard_images/1.jpg")
}

fun Image.toMat(grayscale: Boolean = false): Mat {
    return if (grayscale) toGrayscaleMat() else toRGBMat()
}

private fun Image.toGrayscaleMat(): org.opencv.core.Mat {
    return org.opencv.core.Mat(height, width, CvType.CV_8UC1, planes[0].buffer)
}

private fun Image.toRGBMat(): Mat {
    val rgbaMat = Mat()

    if (format == ImageFormat.YUV_420_888
        && planes.size == 3
    ) {
        val chromaPixelStride = planes[1].pixelStride

        if (chromaPixelStride == 2) { // Chroma channels are interleaved
            assert(planes[0].pixelStride == 1)
            assert(planes[2].pixelStride == 2)
            val yPlane = planes[0].buffer
            val uvPlane1 = planes[1].buffer
            val uvPlane2 = planes[2].buffer
            val yMat = Mat(height, width, CvType.CV_8UC1, yPlane)
            val uvMat1 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane1)
            val uvMat2 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane2)
            val addrDiff = uvMat2.dataAddr() - uvMat1.dataAddr()
            if (addrDiff > 0) {
                assert(addrDiff == 1L)
                Imgproc.cvtColorTwoPlane(yMat, uvMat1, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV12)
            } else {
                assert(addrDiff == -1L)
                Imgproc.cvtColorTwoPlane(yMat, uvMat2, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV21)
            }
        } else { // Chroma channels are not interleaved
            val yuvBytes = ByteArray(width * (height + height / 2))
            val yPlane = planes[0].buffer
            val uPlane = planes[1].buffer
            val vPlane = planes[2].buffer

            yPlane.get(yuvBytes, 0, width * height)

            val chromaRowStride = planes[1].rowStride
            val chromaRowPadding = chromaRowStride - width / 2

            var offset = width * height
            if (chromaRowPadding == 0) {
                // When the row stride of the chroma channels equals their width, we can copy
                // the entire channels in one go
                uPlane.get(yuvBytes, offset, width * height / 4)
                offset += width * height / 4
                vPlane.get(yuvBytes, offset, width * height / 4)
            } else {
                // When not equal, we need to copy the channels row by row
                for (i in 0 until height / 2) {
                    uPlane.get(yuvBytes, offset, width / 2)
                    offset += width / 2
                    if (i < height / 2 - 1) {
                        uPlane.position(uPlane.position() + chromaRowPadding)
                    }
                }
                for (i in 0 until height / 2) {
                    vPlane.get(yuvBytes, offset, width / 2)
                    offset += width / 2
                    if (i < height / 2 - 1) {
                        vPlane.position(vPlane.position() + chromaRowPadding)
                    }
                }
            }

            val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
            yuvMat.put(0, 0, yuvBytes)
            Imgproc.cvtColor(yuvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_I420, 4)
        }
    }
    return rgbaMat
}