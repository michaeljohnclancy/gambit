package com.wadiyatalkinabeet.gambit.android

import android.annotation.SuppressLint
import android.graphics.Point
import androidx.camera.core.*
import com.wadiyatalkinabeet.gambit.CPS
import com.wadiyatalkinabeet.gambit.ml.NeuralLAPS
import com.wadiyatalkinabeet.gambit.yuvToRgba

class ChessboardAnalyzer(private val chessboardListener: (List<Point>) -> Unit, private val neuralLAPS: NeuralLAPS) : ImageAnalysis.Analyzer {

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.yuvToRgba()?.let {
            chessboardListener(CPS(neuralLAPS = neuralLAPS).runLAPS(it)
                .map { point -> Point(point.x.toInt(), point.y.toInt()) })
        }
    }

}