package com.wadiyatalkinabeet.gambit.cv.cornerdetection.v1
//
//import com.wadiyatalkinabeet.gambit.CornerDetector
//import com.wadiyatalkinabeet.gambit.math.geometry.Point
//import com.wadiyatalkinabeet.gambit.math.geometry.Segment
//import com.wadiyatalkinabeet.gambit.ml.NeuralLAPS
//import org.opencv.core.Mat
//import org.opencv.core.Size
//import org.opencv.imgproc.Imgproc
//import kotlin.math.pow
//import kotlin.math.sqrt
//
//class CornerDetectorV1(private val neuralLAPS: NeuralLAPS, private val kernelSize: Int = 10) :
//    CornerDetector {
//
//    fun resize(
//        mat: Mat,
//        numPixels: Float = 500f.pow(2f),
//    ): Pair<Mat, Double> {
//
//        val tmpMat = mat.clone()
//
//        var w: Double = tmpMat.width().toDouble()
//        var h: Double = tmpMat.height().toDouble()
//        val scale: Double = sqrt(numPixels / (w * h))
//        w *= scale
//        h *= scale
//
//        //Swapped on purpose
//        val scaleSize = Size(h,w)
//        Imgproc.resize(tmpMat, tmpMat, scaleSize)
//
//        return Pair(tmpMat, scale)
//    }
//
//    fun detectLines(
//        mat: Mat,
//    ): List<Segment> {
//        return clusterSegments(claheParameters.flatMap { params ->
//            applyHoughLinesP(
//                mat = applyAutoCanny(
//                    mat = applyCLAHE(
//                        mat = mat,
//                        limit = params.first,
//                        gridSize = params.second,
//                        nIterations = params.third
//                    )
//                )
//            )
//        }).map { colinearSegments -> mergeSegments(colinearSegments) }
//    }
//
//    fun detectLatticePoints(
//        mat: Mat,
//        lines: List<Segment>,
//        model: NeuralLAPS,
//        kernelSize: Int = 10,
//    ): List<Point> {
//        return getIntersections(lines)
//            .filter {
//                it.x- kernelSize > 0 && it.x+ kernelSize < mat.width()
//                        && it.y- kernelSize > 0 && it.y+ kernelSize < mat.height()
//            }
//            .map { Pair(it, preprocess(mat.getSubImageAround(it, size = kernelSize))) }
//            .filter { applyGeometricDetector(it.second) || applyNeuralDetector(it.second, model = model) }
//            .map { it.first }
//            .let(::cluster)
//    }
//
//    override fun getCornerPoints(mat: Mat): List<Point> {
//        val (tmpMat, scale) = resize(mat = mat)
//        val lines: List<Segment> = detectLines(tmpMat)
//        val latticePoints = detectLatticePoints(
//            tmpMat,
//            lines = lines,
//            kernelSize = kernelSize,
//            model = neuralLAPS
//        ).map { Point((tmpMat.height() - it.y) / scale, it.x / scale) }
//        // Resize shouldn't be done here
//        throw NotImplementedError()
//    }
//
//    override fun getLines(mat: Mat): List<Segment> {
//        val (tmpMat, scale) = resize(mat = mat)
//        return detectLines(tmpMat)
//    }
//
//    override fun getLatticePoints(mat: Mat): List<Point> {
//        val (tmpMat, scale) = resize(mat = mat)
//        val lines: List<Segment> = detectLines(tmpMat)
//        return detectLatticePoints(
//            tmpMat,
//            lines = lines,
//            kernelSize = kernelSize,
//            model = neuralLAPS
//        )
//    }
//}
