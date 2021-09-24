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
import com.wadiyatalkinabeet.gambit.cv.cornerdetection.v2.InvalidFrameException
import com.wadiyatalkinabeet.gambit.cv.cornerdetection.v2.findCorners
import com.wadiyatalkinabeet.gambit.cv.cornerdetection.v2.findLines
import com.wadiyatalkinabeet.gambit.cv.toMat
import com.wadiyatalkinabeet.gambit.math.datastructures.Line
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.opencv.android.OpenCVLoader
import kotlin.coroutines.coroutineContext

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

//    @SuppressLint("UnsafeOptInUsageError")
//    fun getLatticePoints(): Flow<List<android.graphics.Point>> {
//        return _imageAnalysisUseCaseState.value.analyze().flowOn(Dispatchers.Default)
//            .map { imageProxy ->
//                imageProxy.image?.yuvToRgba()?.let {
//                    imageProxy.close()
//                    cornerDetector.getLatticePoints(it)
//                        .map { point -> Point(point.x.toInt(), point.y.toInt()) }
//                }
//            }.filterNotNull().flowOn(Dispatchers.IO)
//    }

    @SuppressLint("UnsafeOptInUsageError")
    fun getLatticeLines(): Flow<Pair<List<Line>, List<Line>>> =
            _imageAnalysisUseCaseState.value.analyze().flowOn(Dispatchers.Default)
                .map { imageProxy ->
                    imageProxy.image?.toMat()?.let {
                        imageProxy.close()
                        findCorners(it)
                    }
                }.filterNotNull().flowOn(Dispatchers.IO).buffer()
                .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    private fun newImageAnalysisUseCase() = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setTargetAspectRatio(RATIO_4_3)
        .build()
}
