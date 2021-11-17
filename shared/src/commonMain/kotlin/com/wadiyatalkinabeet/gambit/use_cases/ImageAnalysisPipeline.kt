package com.wadiyatalkinabeet.gambit.use_cases

import com.wadiyatalkinabeet.gambit.Resource
import com.wadiyatalkinabeet.gambit.domain.cv.ImageAnalysisState
import com.wadiyatalkinabeet.gambit.domain.cv.Mat
import com.wadiyatalkinabeet.gambit.domain.cv.cornerdetection.v2.detectBoard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

expect class ImageAnalysisPipeline(){
    fun matFlow(grayscale: Boolean): Flow<Mat>
}

operator fun ImageAnalysisPipeline.invoke(): Flow<Resource<ImageAnalysisState>> =
    matFlow(grayscale = true)
        .flowOn(Dispatchers.Default)
        .detectBoard()