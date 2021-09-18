package com.wadiyatalkinabeet.gambit;

import android.annotation.SuppressLint
import android.app.Application;
import android.graphics.Point
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.lifecycle.AndroidViewModel
import com.github.skgmn.cameraxx.analyze
import com.wadiyatalkinabeet.gambit.ml.NeuralLAPS
import kotlinx.coroutines.flow.*

class CameraPreviewViewModel(application:Application): AndroidViewModel(application) {

    val imageAnalysisUseCase = newImageAnalysisUseCase()

    val preview = Preview.Builder().build()

    val permissionsInitiallyRequestedState = MutableStateFlow(false)

    private val chessboardPositionSearch = CPS(neuralLAPS = NeuralLAPS.newInstance(getApplication<Application>()))

    @SuppressLint("UnsafeOptInUsageError")
    fun getLatticePoints() : Flow<List<Point>?>  =
        imageAnalysisUseCase.analyze().map { imageProxy ->
            imageProxy.image?.yuvToRgba()?.let {
                imageProxy.close()
                chessboardPositionSearch.runLAPS(it)
                    .map { point -> Point(point.x.toInt(), point.y.toInt()) }
        }
    }

    private fun newImageAnalysisUseCase() = ImageAnalysis.Builder()
    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    .build()
}