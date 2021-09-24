package com.wadiyatalkinabeet.gambit.cv.cornerdetection.v2

import com.wadiyatalkinabeet.gambit.cv.Mat
import com.wadiyatalkinabeet.gambit.cv.initOpenCV
import com.wadiyatalkinabeet.gambit.cv.loadChessboardExampleImage
import kotlin.test.Test

internal class CornerDetectionKtTest{

    init {
       initOpenCV()
    }

    @Test
    fun findCornersTest(){
        val src: Mat = loadChessboardExampleImage()
        findLines(src)
    }

}