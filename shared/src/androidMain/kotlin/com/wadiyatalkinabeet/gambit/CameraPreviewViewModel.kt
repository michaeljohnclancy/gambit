package com.wadiyatalkinabeet.gambit;

import android.annotation.SuppressLint
import android.util.Size
import androidx.camera.core.AspectRatio.RATIO_4_3
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.lifecycle.ViewModel
import com.github.skgmn.cameraxx.analyze
import com.wadiyatalkinabeet.gambit.domain.cv.ImageAnalysisState
import com.wadiyatalkinabeet.gambit.domain.cv.toMat
import com.wadiyatalkinabeet.gambit.use_cases.DetectBoardUseCase
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

    private var  _detectBoardUseCaseState: MutableStateFlow<DetectBoardUseCase> =
        MutableStateFlow(DetectBoardUseCase())
    val detectBoardUseCaseState: StateFlow<DetectBoardUseCase> = _detectBoardUseCaseState

    private var _imageAnalysisResolution = MutableStateFlow(Size(640, 480))
    val imageAnalysisResolution = _imageAnalysisResolution

    val preview = Preview.Builder()
        .setTargetAspectRatio(RATIO_4_3)
        .build()

    val permissionsInitiallyRequestedState = MutableStateFlow(false)

    @SuppressLint("UnsafeOptInUsageError")
    fun getImageAnalysisResult(): Flow<Resource<ImageAnalysisState>> {
        return _imageAnalysisUseCaseState.value.analyze().flowOn(Dispatchers.Default)
            .map { imageProxy -> imageProxy.image?.toMat(grayscale = true)?.also { imageProxy.close() } }
            .filterNotNull()
            .map { detectBoardUseCaseState.value(it) }
            .flowOn(Dispatchers.IO)
    }

    private fun newImageAnalysisUseCase() = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setTargetAspectRatio(RATIO_4_3)
        .build()
}
