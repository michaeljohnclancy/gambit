package com.wadiyatalkinabeet.gambit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.wadiyatalkinabeet.gambit.use_cases.ImageAnalysisPipeline
import kotlinx.coroutines.flow.*
import org.opencv.android.OpenCVLoader

class CameraPreviewViewModel(application: Application): AndroidViewModel(application){

    init {
        OpenCVLoader.initDebug()
    }

    private var _imageAnalysisPipeline: MutableStateFlow<ImageAnalysisPipeline> =
        MutableStateFlow(ImageAnalysisPipeline())
    val imageAnalysisPipeline: StateFlow<ImageAnalysisPipeline> = _imageAnalysisPipeline

    var permissionsInitiallyRequestedState: MutableStateFlow<Boolean> = MutableStateFlow(false)
}


