package com.wadiyatalkinabeet.gambit

import android.annotation.SuppressLint
import android.util.Size
import androidx.camera.core.AspectRatio.RATIO_4_3
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.lifecycle.ViewModel
import com.github.skgmn.cameraxx.analyze
import com.wadiyatalkinabeet.gambit.domain.cv.ImageAnalysisState
import com.wadiyatalkinabeet.gambit.domain.cv.Mat
import com.wadiyatalkinabeet.gambit.domain.cv.toMat
import com.wadiyatalkinabeet.gambit.use_cases.detectBoardUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.opencv.android.OpenCVLoader

class CameraPreviewViewModel: ViewModel(){

    init {
        OpenCVLoader.initDebug()
    }

    private var _imageAnalysisUseCaseState: MutableStateFlow<ImageAnalysis> =
        MutableStateFlow(newImageAnalysisUseCase())
    val imageAnalysisUseCaseState: StateFlow<ImageAnalysis> = _imageAnalysisUseCaseState

    private var _imageAnalysisResolution = MutableStateFlow(Size(640, 480))
    val imageAnalysisResolution = _imageAnalysisResolution

    val preview = Preview.Builder()
        .setTargetAspectRatio(RATIO_4_3)
        .build()

    val permissionsInitiallyRequestedState = MutableStateFlow(false)

    fun getImageAnalysisResult(): Flow<Resource<ImageAnalysisState>> =
        _imageAnalysisUseCaseState.value
            .toMatFlow(grayscale = true)
            .filterNotNull()
            .flowOn(Dispatchers.Default)
            .detectBoardUseCase()
            .flowOn(Dispatchers.IO)

    @SuppressLint("UnsafeOptInUsageError")
    private fun ImageAnalysis.toMatFlow(grayscale: Boolean = true): Flow<Mat?> =
        analyze { imageProxy ->
            imageProxy.image
                ?.toMat(grayscale = grayscale) ?: null
                .also { imageProxy.close() }
        }

    private fun newImageAnalysisUseCase() = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setTargetAspectRatio(RATIO_4_3)
        .build()
}
