package com.wadiyatalkinabeet.gambit.cornerDetection.v1

import com.wadiyatalkinabeet.gambit.ml.NeuralLAPS
import com.wadiyatalkinabeet.gambit.utils.FCluster
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

internal fun getIntersections(segments: List<Segment>): List<Point> {
    return IntRange(1, segments.size - 1)
        .flatMap { i -> segments.indices.map { i - 1 to it } }
        .mapNotNull { intersectionPoint(segments[it.first], segments[it.second]) }
        .toList()
}

internal fun preprocess(mat: Mat): Mat {
    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
    Imgproc.threshold(mat, mat, 0.0, 255.0, Imgproc.THRESH_OTSU)
    Imgproc.Canny(mat, mat, 0.0, 255.0)
    Imgproc.resize(mat, mat, Size(21.0, 21.0), 0.0, 0.0, Imgproc.INTER_CUBIC)

    return mat
}

internal fun applyGeometricDetector(mat: Mat): Boolean {

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

    val c = Mat(23, 23, CV_8UC3)

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

internal fun applyNeuralDetector(mat: Mat, model: NeuralLAPS): Boolean {
    Imgproc.threshold(mat, mat, 127.0, 255.0, Imgproc.THRESH_BINARY)

    val arr = FloatArray(mat.rows() * mat.cols() * mat.channels())
    for (i in 0..20) {
        for (j in 0..20) {
            arr[j + i * 21] = mat.get(i, j)[0].toFloat() / 255f
        }
    }

    val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 21, 21, 1), DataType.FLOAT32)
    inputFeature0.loadArray(arr)

    val outputs = model.process(inputFeature0)
    val outputFeature0 = outputs.probabilityAsTensorBuffer.floatArray

    return outputFeature0[0] > outputFeature0[1] && outputFeature0[1] < 0.03 && outputFeature0[0] > 0.975
}

internal fun cluster(points: List<Point>, maxDistance: Double = 10.0): List<Point> {
    val pointsArray = points.map { doubleArrayOf(it.x, it.y) }.toTypedArray()
    val a = FCluster.apply(points.size) { index1, index2 ->
        distance(points[index1], points[index2]) <= maxDistance
    }
    //TODO Unsafe kinda (but is functional), fix underlying datastructure FCluster
    return a.map { Point(pointsArray[it?.first()!!][0], pointsArray[it.first()!!][1]) }
}
