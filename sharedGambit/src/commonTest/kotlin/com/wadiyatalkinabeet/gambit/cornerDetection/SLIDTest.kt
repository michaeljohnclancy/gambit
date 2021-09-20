package com.wadiyatalkinabeet.gambit

import kotlin.test.Test
import org.opencv.android.OpenCVLoader

import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import ru.ifmo.ctddev.igushkin.cg.geometry.Segment
import kotlin.test.assertEquals


class SLIDTest {

    @Test
    fun testResized() {
        val testImage = loadChessboardExampleImage()
        val (resizedImage, newSize, scale) =  CPS.resize(testImage)
        Imgcodecs.imwrite("src/commonTest/res/example_chessboard_images/resized.jpg", resizedImage)
    }

    @Test
    fun apply_SLID() {
        val testImage = loadChessboardExampleImage()
        val (resizedImage, newSize, scale) =  CPS.resize(testImage)
        val pslidLines = SLID().analyze(resizedImage)

        resizedImage.applyLines(pslidLines)
        Imgcodecs.imwrite("src/commonTest/res/example_chessboard_images/SLID.jpg", resizedImage)
    }

    private fun loadChessboardExampleImage(): Mat {
        OpenCVLoader.initDebug()
        return Imgcodecs.imread("src/commonTest/res/example_chessboard_images/1.jpg")
    }

    @Test
    fun ifSimilarSegmentsProvided_thenReturnsTrue(){
        val segment1: Segment = Segment(50.0, 130.0, 90.0, 130.0)
        val segment2: Segment = Segment(100.0, 130.0, 120.0, 132.0)
        val segment3: Segment = Segment(50.0, 130.0, 5.0, 20.0)
        assert(segment1.isSimilarTo(segment2))
        assert(!segment1.isSimilarTo(segment3))
    }

    @Test
    fun ifDissimilarSegmentsProvided_thenReturnsFalse(){
        val segment1 = Segment(130.0, 50.0, 130.0, 80.0)
        val segment2 = Segment(130.0, 80.0, 50.0,  130.0)
        assert(!segment1.isSimilarTo(segment2))
    }
}