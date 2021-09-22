package com.wadiyatalkinabeet.gambit

import org.opencv.imgproc.Imgproc
import kotlin.test.Test

class PlatformTest {

    @Test
    fun platformTest(){
        val matIn = loadChessboardExampleImage()
        val matOut = Mat()
        processImage(matIn, matOut, 10.0, 40.0, 5)
    }

}