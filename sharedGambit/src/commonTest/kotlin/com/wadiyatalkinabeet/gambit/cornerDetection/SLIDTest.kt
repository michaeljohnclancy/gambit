package com.wadiyatalkinabeet.gambit.cornerDetection

import kotlin.test.Test
import org.opencv.core.Mat
import kotlin.test.Ignore

class SLIDTest {

    @Ignore
    @Test
    fun ifPassedSyntheticImage_thenSLIDReturnsExpectedLines(){
        val mat: Mat = loadChessboardExampleImage()
        val lines = SLID().analyze(mat)

        // Read in synthetic lines here, generated/manually produced alongside the mat above.
    }

}