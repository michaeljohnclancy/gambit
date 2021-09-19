package com.wadiyatalkinabeet.gambit

import com.wadiyatalkinabeet.gambit.ml.NeuralLAPS
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.core.Core.BORDER_CONSTANT
import org.opencv.core.CvType.CV_8UC3
import org.opencv.imgproc.Imgproc
import ru.ifmo.ctddev.igushkin.cg.geometry.Point
import ru.ifmo.ctddev.igushkin.cg.geometry.Segment
import ru.ifmo.ctddev.igushkin.cg.geometry.intersectionPoint
import org.opencv.core.Mat
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

import ru.ifmo.ctddev.igushkin.cg.geometry.distance


class LAPS (private var model: NeuralLAPS){


    init {
        OpenCVLoader.initDebug()
    }

    private fun getIntersections(segments: List<Segment>): List<Point> {
        return IntRange(1, segments.size - 1)
            .flatMap { i -> segments.indices.map { i - 1 to it } }
            .mapNotNull { intersectionPoint(segments[it.first], segments[it.second]) }
            .toList()
    }

    private fun preprocess(mat: Mat): Mat {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
        Imgproc.threshold(mat, mat, 0.0, 255.0, Imgproc.THRESH_OTSU)
        Imgproc.Canny(mat, mat, 0.0, 255.0)
        Imgproc.resize(mat, mat, Size(21.0, 21.0), 0.0, 0.0, Imgproc.INTER_CUBIC)

        return mat
    }

    private fun applyGeometricDetector(mat: Mat): Boolean {

        val tmpMat = Mat()
        Imgproc.dilate(mat, tmpMat, Mat(), org.opencv.core.Point(-1.0, 1.0), 1)

        val mask = Mat()
        Core.copyMakeBorder(
            tmpMat, mask, 1, 1, 1, 1,
            BORDER_CONSTANT, Scalar(255.0, 255.0, 255.0)
        )

        Core.bitwise_not(mask, mask)

        val contours: List<MatOfPoint> = arrayListOf()
        val hierarchy = Mat()

        Imgproc.findContours(
            mask, contours, hierarchy,
            Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE
        )

        var c = Mat(23, 23, CV_8UC3)

        var count = 0
        for (contour in contours) {
            val centre = Point()
            val radius = FloatArray(1)
            val contour2F = MatOfPoint2f(*contour.toArray())

            Imgproc.minEnclosingCircle(contour2F, centre, radius)

            val approxCurve = MatOfPoint2f()
            Imgproc.approxPolyDP(
                contour2F, approxCurve,
                0.1 * Imgproc.arcLength(contour2F, true),
                true
            )

            if (approxCurve.rows() == 4 && radius[0] < 14.0) {
                Imgproc.drawContours(c, listOf(contour), 0, Scalar(0.0, 255.0, 0.0), 1)
                count++
            } else {
                Imgproc.drawContours(c, listOf(contour), 0, Scalar(0.0, 0.0, 255.0), 1)
            }

        }
        return (count == 4)
    }

    private fun applyNeuralDetector(mat: Mat): Boolean {
        Imgproc.threshold(mat, mat, 127.0, 255.0, Imgproc.THRESH_BINARY)

        // Creates inputs for reference.


        val arr = FloatArray(mat.rows() * mat.cols() * mat.channels())

        for (i in 0..20){
            for (j in 0..20){
                arr[j + i*21] = mat.get(i, j)[0].toFloat() / 255f
            }
        }

//        val byteBuffer: ByteBuffer = ByteBuffer.wrap(bytes)

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 21, 21, 1), DataType.FLOAT32)
//        val probabilityProcessor = TensorProcessor.Builder().add(
//            QuantizeOp(
//                0F,
//                (1 / 255.0).toFloat()
//            )
//        ).build()

//        val quantizedBuffer = probabilityProcessor.process(inputFeature0)

//        quantizedBuffer.loadBuffer(byteBuffer)
        inputFeature0.loadArray(arr)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.probabilityAsTensorBuffer.floatArray

        // Releases model resources if no longer used.
        return outputFeature0[0] > outputFeature0[1] && outputFeature0[1] < 0.03 && outputFeature0[0] > 0.975
    }

    private fun cluster(points: List<Point>, maxDistance: Double = 10.0): List<Point> {
        val pointsArray = points.map { doubleArrayOf(it.x, it.y) }.toTypedArray()
        val a = FCluster.apply(points.size) {
                index1, index2 -> distance( points[index1], points[index2] ) <= maxDistance
        }
        //TODO Unsafe kinda (but is functional), fix underlying datastructure FCluster
        return a.map { Point(pointsArray[it?.first()!!][0], pointsArray[it.first()!!][1]) }
    }

    fun analyze(mat: Mat, segments: List<Segment>, kernelSize: Int = 10): List<Point> {
        return getIntersections(segments)
            .filter {
                it.x-kernelSize > 0 && it.x+kernelSize < mat.width()
                && it.y-kernelSize > 0 && it.y+kernelSize < mat.height()
            }
            .map { Pair(it, preprocess(mat.getSubImageAround(it, size = kernelSize))) }
            .filter { applyGeometricDetector(it.second) || applyNeuralDetector(it.second) }
            .map { it.first }
            .let(::cluster)
    }

//    private fun convertBitmapToByteBuffer(bp: Bitmap): ByteBuffer? {
//        val imgData = ByteBuffer.allocateDirect(BYTES * 60 * 60 * 3)
//        imgData.order(ByteOrder.nativeOrder())
//        val bitmap = Bitmap.createScaledBitmap(bp, 60, 60, true)
//        val intValues = IntArray(60 * 60)
//        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
//
//        // Convert the image to floating point.
//        var pixel = 0
//        for (i in 0..59) {
//            for (j in 0..59) {
//                val `val` = intValues[pixel++]
//                imgData.putFloat((`val` shr 16 and 0xFF) / 255f)
//                imgData.putFloat((`val` shr 8 and 0xFF) / 255f)
//                imgData.putFloat((`val` and 0xFF) / 255f)
//            }
//        }
//        return imgData
//    }
}