package com.wadiyatalkinabeet.gambit

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wadiyatalkinabeet.gambit.CPS.Companion.resize
import com.wadiyatalkinabeet.gambit.ml.NeuralLAPS

import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LAPSTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun analyze() {
        val testImage = loadChessboardExampleImage()
        val (resizedImage, newSize, scale) = resize(testImage)
        val slidLines = SLID().analyze(resizedImage)

        val model = NeuralLAPS.newInstance(context)
        val lapsPoints =  LAPS(model).analyze(resizedImage, slidLines)

        val latticeMat = resizedImage.clone()
        latticeMat.applyPoints(lapsPoints)
        Imgcodecs.imwrite("src/commonTest/res/laps/laps.jpg", latticeMat)
    }

    @Test
    fun preprocess() {
    }

}

fun loadChessboardExampleImage(): Mat {
    OpenCVLoader.initDebug()
    return Imgcodecs.imread("src/commonTest/res/example_chessboard_images/1.jpg")
}