package com.wadiyatalkinabeet.gambit.domain.cv

import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Point

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

class NotEnoughFeaturesException(message: String) : Exception(message)

