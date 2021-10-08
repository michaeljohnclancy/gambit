package com.wadiyatalkinabeet.gambit;

import android.annotation.SuppressLint
import android.app.Application;
import android.util.Size
import androidx.camera.core.AspectRatio.RATIO_4_3
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.skgmn.cameraxx.analyze
import com.wadiyatalkinabeet.gambit.cv.ImageAnalysisResult
import com.wadiyatalkinabeet.gambit.cv.ImageAnalysisState
import com.wadiyatalkinabeet.gambit.cv.findCorners
import com.wadiyatalkinabeet.gambit.cv.toMat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.opencv.android.OpenCVLoader

class CameraPreviewViewModel(application: Application) : AndroidViewModel(application) {

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

    @SuppressLint("UnsafeOptInUsageError")
    fun getImageAnalysisResult(): Flow<ImageAnalysisResult?> {
        return _imageAnalysisUseCaseState.value.analyze().flowOn(Dispatchers.Default)
            .map { imageProxy -> imageProxy.image?.toMat(grayscale = true)?.also { imageProxy.close() } }
            .filterNotNull()
            .map { ImageAnalysisState(it).findCorners() }
            .flowOn(Dispatchers.IO).shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    }

    private fun newImageAnalysisUseCase() = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setTargetAspectRatio(RATIO_4_3)
        .build()
}
