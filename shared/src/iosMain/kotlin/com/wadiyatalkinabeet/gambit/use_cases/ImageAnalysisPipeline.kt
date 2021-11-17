package com.wadiyatalkinabeet.gambit.use_cases

import com.wadiyatalkinabeet.gambit.domain.cv.Mat
import kotlinx.coroutines.flow.Flow

actual class ImageAnalysisPipeline {

    @Throws(NotImplementedError::class)
    actual fun matFlow(grayscale: Boolean): Flow<Mat> {
        throw NotImplementedError("Not Implemented")
    }
}


