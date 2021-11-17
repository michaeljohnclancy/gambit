package com.wadiyatalkinabeet.gambit.domain.cv.cornerdetection.v2

import com.wadiyatalkinabeet.gambit.Resource
import com.wadiyatalkinabeet.gambit.domain.cv.ImageAnalysisState
import com.wadiyatalkinabeet.gambit.domain.cv.Mat
import com.wadiyatalkinabeet.gambit.domain.cv.NotEnoughFeaturesException
import com.wadiyatalkinabeet.gambit.domain.cv.TooManyFeaturesException
import com.wadiyatalkinabeet.gambit.domain.math.algorithms.RANSACException
import com.wadiyatalkinabeet.gambit.domain.math.algorithms.runRANSAC
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withTimeout

const val MIN_LINE_COUNT = 2
const val MAX_LINE_COUNT = 400
const val MIN_POST_LINE_COUNT = 2
const val MIN_INTERSECTIONS = 4

// Timeouts in milliseconds
const val RANSAC_TIMEOUT = 10_000L

fun Flow<Mat>.detectBoard(): Flow<Resource<ImageAnalysisState>> = transform { sourceMat ->
    val imageAnalysisState = ImageAnalysisState(sourceMat)

    try {
        val detectedLines = detectLines(sourceMat, eliminateDiagonals = false)
        if (detectedLines.size < MIN_LINE_COUNT) {
            emit(
                Resource.Error(
                    NotEnoughFeaturesException("Not enough lines found"),
                    imageAnalysisState
                )
            )
            return@transform
        }
        else if (detectedLines.size > MAX_LINE_COUNT) {
            emit(
                Resource.Error(
                    TooManyFeaturesException("Too many lines found"),
                    imageAnalysisState
                )
            )
            return@transform
        }
        emit(
            Resource.Loading(
                imageAnalysisState
            )
        )

        var (horizontalLines, verticalLines) = clusterLines(detectedLines)
        horizontalLines = eliminateSimilarLines(horizontalLines, verticalLines)
        verticalLines = eliminateSimilarLines(verticalLines, horizontalLines)
        if (horizontalLines.size < MIN_POST_LINE_COUNT  || verticalLines.size < MIN_POST_LINE_COUNT) {
            emit(
                Resource.Error(
                    NotEnoughFeaturesException("Not enough lines after elimination"),
                    imageAnalysisState
                )
            )
            return@transform
        }
        imageAnalysisState.horizontalLines = horizontalLines
        imageAnalysisState.verticalLines = verticalLines
        emit(
            Resource.Loading(
                imageAnalysisState
            )
        )

        val allIntersectionPoints = findIntersectionPoints(horizontalLines, verticalLines)
        if (allIntersectionPoints.size * allIntersectionPoints[0].size < MIN_INTERSECTIONS) {
            emit(
                Resource.Error(
                    NotEnoughFeaturesException("Not enough intersection points"),
                    imageAnalysisState
                )
            )
            return@transform
        }
        emit(
            Resource.Loading(
                imageAnalysisState
            )
        )

        val ransacResults = try {
            withTimeout(RANSAC_TIMEOUT) { runRANSAC(allIntersectionPoints) }
        } catch (e: RANSACException) {
            null
        }

        imageAnalysisState.cornerPoints = ransacResults?.let {
            detectCorners(it, sourceMat, imageAnalysisState.scale)
        } ?: run {
            emit(
                Resource.Error(
                    NotEnoughFeaturesException("Detect corners failed"),
                    imageAnalysisState
                )
            )
            return@transform
        }

        emit(
            Resource.Success(
                imageAnalysisState
            )
        )
    } catch(e: TimeoutCancellationException) {
        emit(
            Resource.Error(
                TooManyFeaturesException("Processing took too long"),
                imageAnalysisState
            )
        )
        return@transform
    }
}
