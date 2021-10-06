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
import com.wadiyatalkinabeet.gambit.cv.Point
import com.wadiyatalkinabeet.gambit.cv.cornerdetection.v2.findCorners
import com.wadiyatalkinabeet.gambit.cv.toMat
import com.wadiyatalkinabeet.gambit.math.datastructures.Line
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeoutOrNull
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

//    private val neuralLAPS: NeuralLAPS = NeuralLAPS.newInstance(getApplication<Application>())

//    private val cornerDetector: CornerDetector = CornerDetectorV1(neuralLAPS = neuralLAPS)

    //Close the model in the activity or similar using: neuralLAPS.close()


    @SuppressLint("UnsafeOptInUsageError")
    fun getCorners(): Flow<List<com.wadiyatalkinabeet.gambit.math.datastructures.Point?>?> {
        return _imageAnalysisUseCaseState.value.analyze().flowOn(Dispatchers.Default)
            .map { imageProxy -> imageProxy.image?.toMat()?.also { imageProxy.close() } }
            .filterNotNull()
            .map { mat -> withTimeoutOrNull(5000) { findCorners(mat) } }
            .filterNotNull()
            .flowOn(Dispatchers.IO).shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    }

//    @SuppressLint("UnsafeOptInUsageError")
//    fun getLatticeLines(): Flow<Pair<List<Line>, List<Line>>?> {
//        return _imageAnalysisUseCaseState.value.analyze().flowOn(Dispatchers.Default)
//            .map { imageProxy -> imageProxy.image?.toMat()?.also { imageProxy.close() } }
//            .filterNotNull()
//            .map { mat -> withTimeoutOrNull(timeMillis = 10000) { findCorners(mat) } }
//            .flowOn(Dispatchers.IO).shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
//    }

    private fun newImageAnalysisUseCase() = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setTargetAspectRatio(RATIO_4_3)
        .build()
}
