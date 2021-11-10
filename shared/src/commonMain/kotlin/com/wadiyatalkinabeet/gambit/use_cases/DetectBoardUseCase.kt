package com.wadiyatalkinabeet.gambit.use_cases

import com.wadiyatalkinabeet.gambit.Resource
import com.wadiyatalkinabeet.gambit.domain.cv.*
import com.wadiyatalkinabeet.gambit.domain.cv.cornerdetection.v2.*
import com.wadiyatalkinabeet.gambit.domain.math.algorithms.RANSACException
import com.wadiyatalkinabeet.gambit.domain.math.algorithms.runRANSAC
import kotlinx.coroutines.flow.*

fun Flow<Mat>.detectBoardUseCase(): Flow<Resource<ImageAnalysisState>> = transform { sourceMat ->
    val imageAnalysisState = ImageAnalysisState(sourceMat)

    val detectedLines = detectLines(sourceMat, eliminateDiagonals = false)
    if (detectedLines.size > 2) {
        emit(
            Resource.Loading(
                imageAnalysisState
            )
        )
    } else {
        emit(
            Resource.Error(
                NotEnoughFeaturesException("Not enough lines found!"),
                imageAnalysisState
            )
        )
        return@transform
    }

    var (horizontalLines, verticalLines) = clusterLines(detectedLines)

    horizontalLines = eliminateSimilarLines(horizontalLines, verticalLines)
    verticalLines = eliminateSimilarLines(verticalLines, horizontalLines)
    if (horizontalLines.size > 2 && verticalLines.size > 2) {
        imageAnalysisState.horizontalLines = horizontalLines
        imageAnalysisState.verticalLines = verticalLines
        emit(
            Resource.Loading(
                imageAnalysisState
            )
        )
    } else {
        emit(
            Resource.Error(
                NotEnoughFeaturesException("Not enough lines after elimination!"),
                imageAnalysisState
            )
        )
        return@transform
    }

    val allIntersectionPoints = findIntersectionPoints(horizontalLines, verticalLines)
    if (allIntersectionPoints.size * allIntersectionPoints[0].size >= 4) {
        emit(
            Resource.Loading(
                imageAnalysisState
            )
        )
    } else {
        emit(
            Resource.Error(
                NotEnoughFeaturesException("Not enough intersection points! (< 4)"),
                imageAnalysisState
            )
        )
        return@transform
    }

    val ransacResults = try {
        runRANSAC(allIntersectionPoints)
    } catch (e: RANSACException) {
        null
    }

    ransacResults?.let {
        imageAnalysisState.cornerPoints = detectCorners(ransacResults, sourceMat, imageAnalysisState.scale)
        emit(
            Resource.Success(
                imageAnalysisState
            )
        )
    } ?: run {
        emit(
            Resource.Error(
                NotEnoughFeaturesException("RANSAC failed!"),
                imageAnalysisState
            )
        )
        return@transform
    }
}