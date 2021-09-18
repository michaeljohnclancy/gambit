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
import java.nio.ByteBuffer

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

        Imgproc.dilate(mat, mat, Mat(), org.opencv.core.Point(-1.0, 1.0), 1)

        val mask = Mat()
        Core.copyMakeBorder(
            mat, mask, 1, 1, 1, 1,
            BORDER_CONSTANT, Scalar(255.0, 255.0, 255.0)
        )

        Core.bitwise_not(mask, mask)

        var contours: List<MatOfPoint> = arrayListOf()
        var hierarchy = Mat()

        Imgproc.findContours(
            mask, contours, hierarchy,
            Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE
        )

        var c = Mat(23, 23, CV_8UC3)

        var count = 0
        for (contour in contours) {
            val centre = Point()
            val radius = FloatArray(1)
            var contour2F = MatOfPoint2f(*contour.toArray())

            Imgproc.minEnclosingCircle(contour2F, centre, radius)

            var approxCurve = MatOfPoint2f()
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

        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 21, 21, 1), DataType.FLOAT32)


        var byteArray = ByteArray((mat.width() * mat.height() * mat.channels()))
        mat.get(0, 0, byteArray)

        inputFeature0.loadBuffer(ByteBuffer.wrap(byteArray))

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        // Releases model resources if no longer used.
        model.close()

        return false
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
//            .filter { applyGeometricDetector(it.second) || applyNeuralDetector(it.second) }
            .filter { applyGeometricDetector(it.second) }
            .map { it.first }
            .let(::cluster)
    }

}