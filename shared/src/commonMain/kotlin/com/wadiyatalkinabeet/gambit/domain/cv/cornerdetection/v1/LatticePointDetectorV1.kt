package com.wadiyatalkinabeet.gambit.domain.cv.cornerdetection.v1
//
//import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Segment
//import com.wadiyatalkinabeet.gambit.ml.NeuralLAPS
//import com.wadiyatalkinabeet.gambit.utils.FCluster
//import org.opencv.core.*
//import org.opencv.core.Core.BORDER_CONSTANT
//import org.opencv.core.CvType.CV_8UC3
//import org.opencv.imgproc.Imgproc
//import ru.ifmo.ctddev.igushkin.cg.geometry.intersectionPoint
//import org.opencv.core.Mat
//import org.tensorflow.lite.DataType
//import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
//
//import ru.ifmo.ctddev.igushkin.cg.geometry.distance
//
//internal fun getIntersections(segments: List<Segment>): List<Point> {
//    return IntRange(1, segments.size - 1)
//        .flatMap { i -> segments.indices.map { i - 1 to it } }
//        .mapNotNull { intersectionPoint(segments[it.first], segments[it.second]) }
//        .toList()
//}
//
//internal fun preprocess(mat: Mat): Mat {
//    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
//    Imgproc.threshold(mat, mat, 0.0, 255.0, Imgproc.THRESH_OTSU)
//    Imgproc.Canny(mat, mat, 0.0, 255.0)
//    Imgproc.resize(mat, mat, Size(21.0, 21.0), 0.0, 0.0, Imgproc.INTER_CUBIC)
//
//    return mat
//}
//
//
//internal fun applyNeuralDetector(mat: Mat, model: NeuralLAPS): Boolean {
//    Imgproc.threshold(mat, mat, 127.0, 255.0, Imgproc.THRESH_BINARY)
//
//    val arr = FloatArray(mat.rows() * mat.cols() * mat.channels())
//    for (i in 0..20) {
//        for (j in 0..20) {
//            arr[j + i * 21] = mat.get(i, j)[0].toFloat() / 255f
//        }
//    }
//
//    val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 21, 21, 1), DataType.FLOAT32)
//    inputFeature0.loadArray(arr)
//
//    val outputs = model.process(inputFeature0)
//    val outputFeature0 = outputs.probabilityAsTensorBuffer.floatArray
//
//    return outputFeature0[0] > outputFeature0[1] && outputFeature0[1] < 0.03 && outputFeature0[0] > 0.975
//}
//
//internal fun cluster(points: List<Point>, maxDistance: Double = 10.0): List<Point> {
//    val pointsArray = points.map { doubleArrayOf(it.x, it.y) }.toTypedArray()
//    val a = FCluster.apply(points.size) { index1, index2 ->
//        distance(points[index1], points[index2]) <= maxDistance
//    }
//    //TODO Unsafe kinda (but is functional), fix underlying datastructure FCluster
//    return a.map { Point(pointsArray[it?.first()!!][0], pointsArray[it.first()!!][1]) }
//}
