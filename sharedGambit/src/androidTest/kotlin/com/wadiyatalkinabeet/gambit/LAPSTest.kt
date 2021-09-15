package com.wadiyatalkinabeet.gambit

import org.junit.Assert.*

import org.junit.Test
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs

class LAPSTest {

    @Test
    fun analyze() {
        val testImage = loadChessboardExampleImage()
        val (resizedImage, newSize, scale) =  resize(testImage)
        val slidLines = SLID().analyze(resizedImage)
        val lapsPoints =  LAPS().analyze(resizedImage, slidLines)

        val latticeMat = resizedImage.clone()
        latticeMat.applyPoints(lapsPoints)
        Imgcodecs.imwrite("src/commonTest/res/laps/laps.jpg", latticeMat)
    }

    @Test
    fun preprocess() {
    }

    private fun loadChessboardExampleImage(): Mat {
        OpenCVLoader.initDebug()
        return Imgcodecs.imread("src/commonTest/res/example_chessboard_images/1.jpg")
    }
}