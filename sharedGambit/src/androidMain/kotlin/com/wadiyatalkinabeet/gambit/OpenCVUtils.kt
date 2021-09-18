package com.wadiyatalkinabeet.gambit

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import ru.ifmo.ctddev.igushkin.cg.geometry.Point
import ru.ifmo.ctddev.igushkin.cg.geometry.Segment
import ru.ifmo.ctddev.igushkin.cg.geometry.distance
import ru.ifmo.ctddev.igushkin.cg.geometry.distanceToLine
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

fun Mat.getSubImageAround(point: Point, size: Int): Mat {
    //TODO The geometry library is returning negative values for the intersections of lines.
//    val lx1 = max(0, min(width(), (point.x - size - 1).toInt()))
//    val ly1 = max(0, min(height(), (point.y - size).toInt()))
//    val lx2 = max(0, min(width(), (point.x + size).toInt()))
//    val ly2 = max(0, min(height(), (point.y + size + 1).toInt()))
//    return submat(Range(ly1, ly2), Range(lx1, lx2))
    val lx0 = (point.x - size - 1).toInt()
    val lx1 = (point.x + size).toInt()
    val ly0 = (point.y - size).toInt()
    val ly1 = (point.y + size + 1).toInt() //TODO Why -1 for x but +1 for y?????
    return submat(Range(ly0, ly1), Range(lx0, lx1))
}

fun Bitmap.toMat() : Mat {
    val mat = Mat()
    Utils.bitmapToMat(this, mat)
    return mat
}

fun Mat.toBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    val bitmap = Bitmap.createBitmap(this.cols(), this.rows(), config)
    Utils.matToBitmap(this, bitmap)
    return bitmap
}

fun Bitmap.toDisk(filename: String){
    try {
        FileOutputStream(filename).use { out ->
            this.compress(Bitmap.CompressFormat.PNG, 100, out) // bmp is your Bitmap instance
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun Mat.ravel(): DoubleArray{
    val reshapedMat: Mat = this.reshape(1,1)
    val flattenedArray = DoubleArray(reshapedMat.width())
    for (i in flattenedArray.indices) {
        flattenedArray[i] = reshapedMat[0, i][0]
    }
    return flattenedArray
}

fun Mat.median(): Double{
    var flattenedArray = ravel()
    flattenedArray.sort()
    return median(flattenedArray)
}

fun IntRange.toIntArray(): IntArray {
    if (last < first)
        return IntArray(0)

    val result = IntArray(last - first + 1)
    var index = 0
    for (element in this)
        result[index++] = element
    return result
}

fun median(m: DoubleArray): Double {
    val middle = m.size / 2
    return if (m.size % 2 == 1) {
        m[middle]
    } else {
        (m[middle - 1] + m[middle]) / 2.0
    }
}

fun Mat.applyPoints(
    pointList: List<Point>,
    color: Scalar = Scalar(0.0,0.0,255.0),
    size: Int = 10
){
    pointList.forEach {
        Imgproc.circle(this, it.toOpenCV(), 1, color, size, -1)
    }
}

fun Mat.applyLines(
    lineList: List<Segment>,
    color: Scalar = Scalar(0.0, 0.0, 255.0),
    size: Int = 2
){
    lineList.forEach {
        Imgproc.line(this, it.from.toOpenCV(), it.to.toOpenCV(), color, size)
    }
}

fun Point.toOpenCV(): org.opencv.core.Point{
    return org.opencv.core.Point(this.x, this.y)
}

//TODO The length of the line could be cached for performance
fun Segment.length(): Double{
    return distance(this.from, this.to)
}

fun Segment.isSimilarTo(segment2: Segment): Boolean {
    val d1x = distanceTo(segment2.from)
    val d2x = distanceTo(segment2.to)
    val d1y = segment2.distanceTo(from)
    val d2y = segment2.distanceTo(to)

    //FIXME: Why EPSILON?
//    val ds = (d1x + d2x + d1y + d2y) / 4 + EPSILON
    val ds = (d1x + d2x + d1y + d2y) / 4
    val maxError = 0.0625 * (length() + segment2.length())

    return (length() / ds > maxError).and(segment2.length() / ds > maxError)
}

fun generate(segment: Segment, nIterations: Int = 10): List<Point> {
    return IntRange(0, nIterations - 1).map {
        Point(
            x = segment.x0 - (segment.x1 - segment.x0) * (it * (1 / nIterations)),
            y = segment.y0 - (segment.y1 - segment.y0) * (it * (1 / nIterations))
        )
    }
}

fun Segment.distanceTo(point: Point): Double {
    return distanceToLine(point, this)
}

fun Image.yuvToRgba(): Mat {
    val rgbaMat = Mat()

    if (format == ImageFormat.YUV_420_888
        && planes.size == 3) {

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