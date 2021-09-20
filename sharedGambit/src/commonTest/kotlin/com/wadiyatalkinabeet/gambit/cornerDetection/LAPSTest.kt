package com.wadiyatalkinabeet.gambit.cornerDetection

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.wadiyatalkinabeet.gambit.cornerDetection.CPS.Companion.resize
import com.wadiyatalkinabeet.gambit.ml.NeuralLAPS
import kotlin.test.Ignore

import kotlin.test.Test
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs

class LAPSTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Ignore
    @Test
    fun ifPassedSyntheticImage_thenLAPSReturnsExpectedLatticePoints() {
        val testImage = loadChessboardExampleImage()
        val (resizedImage, scale) = resize(testImage)
        val slidLines = SLID().analyze(resizedImage)

        val model = NeuralLAPS.newInstance(context)
        val lapsPoints =  LAPS(model).analyze(resizedImage, slidLines)

        //Compare laps points here with manually produced

//        val latticeMat = resizedImage.clone()
//        latticeMat.applyPoints(lapsPoints)
//        Imgcodecs.imwrite("src/commonTest/res/laps/laps.jpg", latticeMat)
    }
}

fun loadChessboardExampleImage(): Mat {
    OpenCVLoader.initDebug()
    return Imgcodecs.imread("src/commonTest/res/example_chessboard_images/1.jpg")
}