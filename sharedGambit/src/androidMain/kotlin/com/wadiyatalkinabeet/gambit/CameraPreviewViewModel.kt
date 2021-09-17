package com.wadiyatalkinabeet.gambit;

import android.annotation.SuppressLint
import android.app.Application;
import android.graphics.Point
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.lifecycle.AndroidViewModel
import com.wadiyatalkinabeet.gambit.ml.NeuralLAPS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CameraPreviewViewModel(application:Application): AndroidViewModel(application) {
    private val _imageAnalysisState = MutableStateFlow(newImageAnalysis())
    private val _latticePoints = MutableStateFlow<List<Point>>(listOf())
    val latticePoints: StateFlow<List<Point>> = _latticePoints

    val imageAnalysisState = MutableStateFlow(newImageAnalysis())

    val preview = Preview.Builder().build()

    val permissionsInitiallyRequestedState = MutableStateFlow(false)

    val neuralLAPS = NeuralLAPS.newInstance(getApplication<Application>())

    fun onNewLatticePoints(latticePoints: List<Point>){
        _latticePoints.value = latticePoints
    }

    fun replaceImageCapture() {
        _imageAnalysisState.value = newImageAnalysis()
    }

    private fun newImageAnalysis() = ImageAnalysis.Builder().build()
}

class ChessboardAnalyzer(private val neuralLAPS: NeuralLAPS,) : ImageAnalysis.Analyzer {

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.yuvToRgba()?.let {
            CPS(neuralLAPS = neuralLAPS).runLAPS(it)
                .map { point -> Point(point.x.toInt(), point.y.toInt()) }
        }
    }
}
