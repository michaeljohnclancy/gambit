package com.wadiyatalkinabeet.gambit.use_cases

import com.wadiyatalkinabeet.gambit.Resource
import com.wadiyatalkinabeet.gambit.domain.cv.*
import com.wadiyatalkinabeet.gambit.domain.cv.cornerdetection.v2.*
import com.wadiyatalkinabeet.gambit.domain.math.algorithms.RANSACException
import com.wadiyatalkinabeet.gambit.domain.math.algorithms.runRANSAC

class DetectBoardUseCase() {

    operator fun invoke(sourceMat: Mat): Resource<ImageAnalysisState> {
        val imageAnalysisState = ImageAnalysisState(sourceMat)

        val detectedLines = detectLines(sourceMat, eliminateDiagonals = false)
        if (detectedLines.size <= 2) {
            return Resource.Error(
                NotEnoughFeaturesException("Not enough lines found!"),
                imageAnalysisState
            )
        }

        var (horizontalLines, verticalLines) = clusterLines(detectedLines)

        horizontalLines = eliminateSimilarLines(horizontalLines, verticalLines)
        verticalLines = eliminateSimilarLines(verticalLines, horizontalLines)
        if (horizontalLines.size <= 2 || verticalLines.size <= 2) {
            return Resource.Error(
                NotEnoughFeaturesException("Not enough lines after elimination!"),
                imageAnalysisState
            )
        }

        val allIntersectionPoints = findIntersectionPoints(horizontalLines, verticalLines)
        if (allIntersectionPoints.size * allIntersectionPoints[0].size < 4) {
            return Resource.Error(
                NotEnoughFeaturesException("Not enough intersection points! (< 4)"),
                imageAnalysisState
            )
        }

        val ransacResults = try {
            runRANSAC(allIntersectionPoints)
        } catch (e: RANSACException) {
            null
        } ?: run {
            return Resource.Error(
                NotEnoughFeaturesException("RANSAC failed!"),
                imageAnalysisState
            )
        }

        imageAnalysisState.cornerPoints = detectCorners(ransacResults, sourceMat, imageAnalysisState.scale)

        return Resource.Success(imageAnalysisState)
    }
}