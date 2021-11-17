package com.wadiyatalkinabeet.gambit.use_cases

import android.annotation.SuppressLint
import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import com.github.skgmn.cameraxx.analyze
import com.wadiyatalkinabeet.gambit.domain.cv.Mat
import com.wadiyatalkinabeet.gambit.domain.cv.toMat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

actual class ImageAnalysisPipeline{
    val preview = newPreview()
    val imageAnalyzer = newImageAnalyzer()

    private var _imageAnalysisResolution = MutableStateFlow(Size(640, 480))
    val imageAnalysisResolution = _imageAnalysisResolution

    private fun newImageAnalyzer() = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
        .build()

    private fun newPreview() = Preview.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
        .build()

    @SuppressLint("UnsafeOptInUsageError")
    actual fun matFlow(grayscale: Boolean): Flow<Mat> =
        imageAnalyzer.analyze { imageProxy ->
            imageProxy.image
                ?.toMat(grayscale = grayscale)
                .also { imageProxy.close() }
        }.filterNotNull()

    val resultFlow = this().flowOn(Dispatchers.IO)
}
