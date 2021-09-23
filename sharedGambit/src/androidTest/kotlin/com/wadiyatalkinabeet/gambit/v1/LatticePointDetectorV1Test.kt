package com.wadiyatalkinabeet.gambit.v1

//import android.content.Context
//import androidx.test.core.app.ApplicationProvider
//import com.wadiyatalkinabeet.gambit.cornerDetection.v1.CornerDetectorV1
//import com.wadiyatalkinabeet.gambit.cv.loadChessboardExampleImage
//import com.wadiyatalkinabeet.gambit.ml.NeuralLAPS
//import kotlin.test.Ignore

//import kotlin.test.Test
//
//class LatticePointDetectorV1Test {
//
//    private val context: Context = ApplicationProvider.getApplicationContext()
////    private val cornerDetectorV1: CornerDetectorV1 =
////        CornerDetectorV1(NeuralLAPS.newInstance(context.applicationContext))
//
//    @Ignore
//    @Test
//    fun ifPassedSyntheticImage_thenDetectorReturnsExpectedLatticePoints() {
//        val testImage = loadChessboardExampleImage()
//        val (resizedImage, scale) = cornerDetectorV1.resize(testImage)
//        val lines = cornerDetectorV1.detectLines(resizedImage)
//
//        val model = NeuralLAPS.newInstance(context)
//        val lapsPoints = cornerDetectorV1
//            .detectLatticePoints(
//                mat = resizedImage,
//                lines = lines,
//                model = model
//            )
//
//        //Compare laps points here with manually produced
////        val latticeMat = resizedImage.clone()
////        latticeMat.applyPoints(lapsPoints)
////        Imgcodecs.imwrite("src/commonTest/res/laps/laps.jpg", latticeMat)
//    }
//
//    fun ifPassedSyntheticImage_thenDetectorReturnsExpectedLines() {
//        val testImage = loadChessboardExampleImage()
//        val (resizedImage, scale) = cornerDetectorV1.resize(testImage)
//        val lines = cornerDetectorV1.detectLines(resizedImage)
//    }
//}