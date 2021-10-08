package com.wadiyatalkinabeet.gambit.cv

import com.wadiyatalkinabeet.gambit.cv.cornerdetection.v2.*
import com.wadiyatalkinabeet.gambit.math.algorithms.RANSACException
import com.wadiyatalkinabeet.gambit.math.algorithms.runRANSAC
import com.wadiyatalkinabeet.gambit.math.datastructures.Point

//
//data class CornerDetectionState(
//    val sourceMat: Mat,
//    val resizeWidth: Double = 1200.0,
////    val warpedImage: Mat?,
//) {
//
//    private val scale = resizeWidth / sourceMat.width().toDouble()
//
//    private val preprocessedMat = run {
//        val dst = Mat()
//        resize(sourceMat, dst, Size(resizeWidth, sourceMat.height() * scale))
//        dst
//    }
//
//    private val allLines = detectLines(preprocessedMat, eliminateDiagonals = false)
//
//    private val clusteredLines: Pair<List<Line>, List<Line>> =
//        //TODO Use Horizontal eliminated lines as perpendicular input to vertical elimination
//        clusterLines(allLines)
//            .let { (horizontalLines, verticalLines) ->
//                Pair(
//                    eliminateSimilarLines(horizontalLines, verticalLines),
//                    eliminateSimilarLines(verticalLines, horizontalLines)
//                )
//            }
//
//    private val allIntersectionPoints = findIntersectionPoints(clusteredLines.first, clusteredLines.second)
//
//    val ransacResults = runRANSAC(allIntersectionPoints)
//
//    private val bestWarpMatrix = findHomography(
//        MatOfPoint2f(
//            *ransacResults.intersectionPoints.flatten().filterNotNull().toTypedArray()
//        ),
//        MatOfPoint2f(*ransacResults.quantizedPoints.flatten().filterNotNull().toTypedArray())
//    )
//
//    val warpedResizedMat = ransacResults.let {
//        val dst = Mat()
//        warpPerspective(
//            sourceMat, dst,
//            bestWarpMatrix, it.warpedImageSize
//        )
//        dst
//    }
//
//    val cornerPoints = ransacResults.let{ detectCorners(it, sourceMat, scale) }
//
//}

data class ImageAnalysisState(
    val sourceMat: Mat,
    val resizeWidth: Double = 1200.0,
    var cornerPoints: List<Point>? = null,
    var warpedTransformedMat: Mat? = null,
) {
    val scale = resizeWidth / sourceMat.width().toDouble()
}

sealed class ImageAnalysisResult{
    data class Success(val imageAnalysisState: ImageAnalysisState): ImageAnalysisResult()
    data class Failure(val imageAnalysisState: ImageAnalysisState, val exception: Exception): ImageAnalysisResult()

    class NotEnoughFeaturesException(message: String) : Exception(message)
}

fun ImageAnalysisState.findCorners(): ImageAnalysisResult {
    require(sourceMat.type() == CV_8UC1)
    //Resize
    // Detect all lines
    val detectedLines = detectLines(sourceMat, eliminateDiagonals = false)
    if (detectedLines.size <= 2){
//    if (detectedLines.size <= 2 || detectedLines.size >= 800){
        return ImageAnalysisResult.Failure(
            this,
            ImageAnalysisResult.NotEnoughFeaturesException("Not enough lines found!")
        )
    }
    //        .also { if (it.size > 800) return null }

    // Cluster lines into vertical and horizontal

    var (horizontalLines, verticalLines) = clusterLines(detectedLines)

    // Eliminate similar lines within groups
    horizontalLines = eliminateSimilarLines(horizontalLines, verticalLines)
    verticalLines = eliminateSimilarLines(verticalLines, horizontalLines)
    if (horizontalLines.size <= 2 || verticalLines.size <= 2) {
        return ImageAnalysisResult.Failure(
            this,
            ImageAnalysisResult.NotEnoughFeaturesException("Not enough lines after elimination!")
        )
    }

    // Find intersections between remaining points
    val allIntersectionPoints = findIntersectionPoints(horizontalLines, verticalLines)
    if (allIntersectionPoints.size * allIntersectionPoints[0].size < 4) {
        return ImageAnalysisResult.Failure(
            this,
            ImageAnalysisResult.NotEnoughFeaturesException("Not enough intersection points! (< 4)")
        )
    }

    val ransacResults = try {
        runRANSAC(allIntersectionPoints)
    } catch (e: RANSACException) {
        null
    } ?: run {
        return ImageAnalysisResult.Failure(
            this,
            ImageAnalysisResult.NotEnoughFeaturesException("RANSAC failed!")
        )
    }

    cornerPoints = detectCorners(ransacResults, sourceMat, scale)

    return ImageAnalysisResult.Success(this)
}
