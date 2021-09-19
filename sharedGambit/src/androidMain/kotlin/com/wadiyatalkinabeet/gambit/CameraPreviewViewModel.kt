package com.wadiyatalkinabeet.gambit;

import android.annotation.SuppressLint
import android.app.Application;
import android.graphics.Point
import android.util.Size
import androidx.camera.core.AspectRatio.RATIO_4_3
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.lifecycle.AndroidViewModel
import com.github.skgmn.cameraxx.analyze
import com.wadiyatalkinabeet.gambit.ml.NeuralLAPS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class CameraPreviewViewModel(application:Application): AndroidViewModel(application) {

    private var _imageAnalysisUseCaseState: MutableStateFlow<ImageAnalysis> =
        MutableStateFlow(newImageAnalysisUseCase())
    val imageAnalysisUseCaseState: StateFlow<ImageAnalysis> = _imageAnalysisUseCaseState

    private var _imageAnalysisResolution = MutableStateFlow(Size(640,480))
    val imageAnalysisResolution = _imageAnalysisResolution

    val preview = Preview.Builder()
        .setTargetAspectRatio(RATIO_4_3)
        .build()

    val permissionsInitiallyRequestedState = MutableStateFlow(false)

    private val chessboardPositionSearch =
        CPS(neuralLAPS = NeuralLAPS.newInstance(getApplication<Application>()))

    //Close the model in the activity or similar using: neuralLAPS.close()

    @SuppressLint("UnsafeOptInUsageError")
    fun getLatticePoints(): Flow<List<Point>> {
        return _imageAnalysisUseCaseState.value.analyze().flowOn(Dispatchers.Default)
            .map { imageProxy ->
                imageProxy.image?.yuvToRgba()?.let {
                    imageProxy.close()
                    chessboardPositionSearch.runLAPS(it)
                        .map { point -> Point(point.x.toInt(), point.y.toInt()) }
                }
            }.filterNotNull().flowOn(Dispatchers.IO)
    }

    private fun newImageAnalysisUseCase() = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setTargetAspectRatio(RATIO_4_3)
        .build()

//
//    private fun transformPoint(point: Point) {
//
//        val scaleY = previewHeight / height.toFloat()
//
//        // If the front camera lens is being used, reverse the right/left coordinates
//        val flippedLeft = if (isFrontLens) width - right else left
//        val flippedRight = if (isFrontLens) width - left else right
//
//        // Scale all coordinates to match preview
//        val scaledLeft = scaleX * flippedLeft
//        val scaledTop = scaleY * top
//        val scaledRight = scaleX * flippedRight
//        val scaledBottom = scaleY * bottom
//        return RectF(scaledLeft, scaledTop, scaledRight, scaledBottom)
//    }

}
