package com.wadiyatalkinabeet.gambit;

import android.annotation.SuppressLint
import android.app.Application;
import android.graphics.Point
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.lifecycle.AndroidViewModel
import com.github.skgmn.cameraxx.analyze
import com.wadiyatalkinabeet.gambit.ml.NeuralLAPS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class CameraPreviewViewModel(application:Application): AndroidViewModel(application) {

    private var _imageAnalysisUseCaseState: MutableStateFlow<ImageAnalysis> = MutableStateFlow(newImageAnalysisUseCase())
    val imageAnalysisUseCaseState: StateFlow<ImageAnalysis> = _imageAnalysisUseCaseState

    val preview = Preview.Builder().build()

    val permissionsInitiallyRequestedState = MutableStateFlow(false)

    private val chessboardPositionSearch = CPS(neuralLAPS = NeuralLAPS.newInstance(getApplication<Application>()))

    //Close the model in the activity or similar using: neuralLAPS.close()

    @SuppressLint("UnsafeOptInUsageError")
    fun getLatticePoints() : Flow<List<Point>> {
            return _imageAnalysisUseCaseState.value.analyze().flowOn(Dispatchers.Default).map { imageProxy ->
                imageProxy.image?.yuvToRgba()?.let {
                    imageProxy.close()
                    chessboardPositionSearch.runLAPS(it)
                        .map { point -> Point(point.x.toInt(), point.y.toInt()) }
                }
            }.filterNotNull().flowOn(Dispatchers.IO)
    }

    private fun newImageAnalysisUseCase() = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
}