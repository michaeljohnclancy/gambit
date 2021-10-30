package com.wadiyatalkinabeet.gambit.domain.cv

import android.graphics.ImageFormat
import android.media.Image
import org.opencv.android.OpenCVLoader
import org.opencv.calib3d.Calib3d
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.MatOfPoint2f
import org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE
import org.opencv.imgcodecs.Imgcodecs.imread
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer
import kotlin.math.roundToInt

fun initOpenCV() {
    OpenCVLoader.initDebug()
}

actual open class Mat {

    val nativeMat: org.opencv.core.Mat
    actual constructor() {
        nativeMat = org.opencv.core.Mat()
    }
    constructor(nativeMat: org.opencv.core.Mat) {
        this.nativeMat = nativeMat
    }
    actual constructor(width: Int, height: Int, type: Int, byteBuffer: ByteBuffer?) {
        nativeMat = byteBuffer?.let {
            org.opencv.core.Mat(width, height, type, it)
        } ?: org.opencv.core.Mat(width, height, type)
    }

    actual operator fun get(row: Int, col: Int): FloatArray =
        nativeMat.get(row, col).map { it.toFloat() }.toFloatArray()

    actual operator fun set(row: Int, col: Int, value: FloatArray) {
        nativeMat.put(row, col, value)
    }

    actual fun size(): Size = nativeMat.size().let { Size(it.width.roundToInt(), it.height.roundToInt()) }
    actual fun type(): Int = nativeMat.type()
    actual fun width(): Int = nativeMat.width()
    actual fun height(): Int = nativeMat.height()
    actual fun rows(): Int = nativeMat.rows()
    actual fun cols(): Int = nativeMat.cols()

    actual fun reshape(channels: Int, rows: Int): Mat =
        Mat(nativeMat.reshape(channels, rows))
    actual fun convertTo(resultMat: Mat, type: Int) =
        nativeMat.convertTo(resultMat.nativeMat, type)

    actual fun row(rowIndex: Int): Mat = Mat(nativeMat.row(rowIndex))
    actual fun col(colIndex: Int): Mat = Mat(nativeMat.col(colIndex))

    actual companion object {
        actual fun zeros(size: Size, type: Int): Mat = Mat(org.opencv.core.Mat.zeros(size, type))
    }
}

actual class MatOfPoint2 actual constructor(points: List<Point>): MatOfPoint2f(*points.map { it.nativePoint }.toTypedArray())
actual class MatOfPoint3 actual constructor(points: List<Point3>): org.opencv.core.MatOfPoint3f(*points.map { it.nativePoint }.toTypedArray())

actual data class Point actual constructor(actual val x: Float, actual val y: Float) {
    val nativePoint: org.opencv.core.Point = org.opencv.core.Point(x.toDouble(), y.toDouble())
}
actual data class Point3 actual constructor(actual val x: Float, actual val y: Float, actual val z: Float) {
    val nativePoint: org.opencv.core.Point3 = org.opencv.core.Point3(x.toDouble(), y.toDouble(), z.toDouble())
}

actual class Size actual constructor(width: Int, height: Int): org.opencv.core.Size(width.toDouble(), height.toDouble())

actual fun multiply(src1: Mat, src2: Mat, dst: Mat) =
    Core.multiply(src1.nativeMat, src2.nativeMat, dst.nativeMat)

//actual fun gemm(src1: Mat, src2: Mat, alpha: Double, src3: Mat, beta: Double, dst: Mat) = Core.gemm(src1, src2, alpha, src3, beta, dst)
//
//actual fun vector_Point2d_to_Mat(points: List<Point>): Mat = Converters.vector_Point2d_to_Mat(points)

actual fun canny(
    src: Mat,
    dst: Mat,
    lowerThreshold: Double,
    upperThreshold: Double,
) =  Imgproc.Canny(src.nativeMat, dst.nativeMat, lowerThreshold, upperThreshold)

actual fun canny(
    src: Mat,
    dst: Mat,
    lowerThreshold: Double,
    upperThreshold: Double,
    apertureSize: Int
) = Imgproc.Canny(src.nativeMat, dst.nativeMat, lowerThreshold, upperThreshold)

actual fun cvtColor(
    src: Mat,
    dst: Mat,
    colorOut: Int
) = Imgproc.cvtColor(src.nativeMat, dst.nativeMat, colorOut)

actual fun houghLines(
    src: Mat,
    lines: Mat,
    rho: Double,
    theta: Double,
    threshold: Int,
) =  Imgproc.HoughLines(src.nativeMat, lines.nativeMat, rho, theta, threshold)

actual fun houghLinesP(
    src: Mat, lines: Mat,
    rho: Double, theta: Double,
    threshold: Int, minLineLength: Double,
    maxLineGap: Double
) = Imgproc.HoughLinesP(src.nativeMat, lines.nativeMat, rho, theta, threshold, minLineLength, maxLineGap)

actual fun findHomography(
    srcPoints: MatOfPoint2,
    dstPoints: MatOfPoint2
) = Mat(Calib3d.findHomography(srcPoints as MatOfPoint2f, dstPoints as MatOfPoint2f))

actual fun warpPerspective(
    src: Mat,
    dst: Mat,
    transformationMatrix: Mat,
    dsize: Size
) = Imgproc.warpPerspective(src.nativeMat, dst.nativeMat, transformationMatrix.nativeMat, dsize)

actual fun sobel(
    src: Mat,
    dst: Mat,
    ddepth: Int,
    dx: Int,
    dy: Int,
    kernelSize: Int
) = Imgproc.Sobel(src.nativeMat, dst.nativeMat, ddepth, dx, dy, kernelSize)

actual fun medianBlur(
    src: Mat,
    dst: Mat,
    kernelSize: Int
) = Imgproc.medianBlur(src.nativeMat, dst.nativeMat, kernelSize)

actual fun gaussianBlur(
    src: Mat, dst: Mat,
    kernelSize: Size, sigmaX: Double
) = Imgproc.GaussianBlur(src.nativeMat, dst.nativeMat, kernelSize, sigmaX)

actual fun resize(
    src: Mat,
    dst: Mat,
    dsize: Size
) = Imgproc.resize(src.nativeMat, dst.nativeMat, dsize)

fun Image.toMat(grayscale: Boolean = false): Mat {
    return if (grayscale) toGrayscaleMat() else toRGBMat()
}

actual fun imread(path: String) = Mat(imread(path, IMREAD_GRAYSCALE))

private fun Image.toGrayscaleMat(): Mat {
    return Mat(height, width, CvType.CV_8UC1, planes[0].buffer)
}

private fun Image.toRGBMat(): Mat {
    throw NotImplementedError()
}
//private fun Image.toRGBMat(): Mat {
//    val rgbaMat = Mat()
//
//    if (format == ImageFormat.YUV_420_888
//        && planes.size == 3
//    ) {
//        val chromaPixelStride = planes[1].pixelStride
//
//        if (chromaPixelStride == 2) { // Chroma channels are interleaved
//            assert(planes[0].pixelStride == 1)
//            assert(planes[2].pixelStride == 2)
//            val yPlane = planes[0].buffer
//            val uvPlane1 = planes[1].buffer
//            val uvPlane2 = planes[2].buffer
//            val yMat = Mat(height, width, CvType.CV_8UC1, yPlane)
//            val uvMat1 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane1)
//            val uvMat2 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane2)
//            val addrDiff = uvMat2.dataAddr() - uvMat1.dataAddr()
//            if (addrDiff > 0) {
//                assert(addrDiff == 1L)
//                Imgproc.cvtColorTwoPlane(yMat, uvMat1, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV12)
//            } else {
//                assert(addrDiff == -1L)
//                Imgproc.cvtColorTwoPlane(yMat, uvMat2, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV21)
//            }
//        } else { // Chroma channels are not interleaved
//            val yuvBytes = ByteArray(width * (height + height / 2))
//            val yPlane = planes[0].buffer
//            val uPlane = planes[1].buffer
//            val vPlane = planes[2].buffer
//
//            yPlane.get(yuvBytes, 0, width * height)
//
//            val chromaRowStride = planes[1].rowStride
//            val chromaRowPadding = chromaRowStride - width / 2
//
//            var offset = width * height
//            if (chromaRowPadding == 0) {
//                // When the row stride of the chroma channels equals their width, we can copy
//                // the entire channels in one go
//                uPlane.get(yuvBytes, offset, width * height / 4)
//                offset += width * height / 4
//                vPlane.get(yuvBytes, offset, width * height / 4)
//            } else {
//                // When not equal, we need to copy the channels row by row
//                for (i in 0 until height / 2) {
//                    uPlane.get(yuvBytes, offset, width / 2)
//                    offset += width / 2
//                    if (i < height / 2 - 1) {
//                        uPlane.position(uPlane.position() + chromaRowPadding)
//                    }
//                }
//                for (i in 0 until height / 2) {
//                    vPlane.get(yuvBytes, offset, width / 2)
//                    offset += width / 2
//                    if (i < height / 2 - 1) {
//                        vPlane.position(vPlane.position() + chromaRowPadding)
//                    }
//                }
//            }
//
//            val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
//            yuvMat.put(0, 0, yuvBytes)
//            Imgproc.cvtColor(yuvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_I420, 4)
//        }
//    }
//    return rgbaMat
//}