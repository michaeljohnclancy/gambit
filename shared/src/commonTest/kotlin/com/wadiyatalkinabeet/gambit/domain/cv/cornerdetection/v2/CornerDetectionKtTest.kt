package com.wadiyatalkinabeet.gambit.domain.cv.cornerdetection.v2

import com.wadiyatalkinabeet.gambit.domain.cv.Mat
import com.wadiyatalkinabeet.gambit.domain.cv.initOpenCV
import com.wadiyatalkinabeet.gambit.domain.cv.loadChessboardExampleImage
import kotlin.test.Test

internal class CornerDetectionKtTest{

    init {
       initOpenCV()
    }

    @Test
    fun findCornersTest(){
        val src: Mat = loadChessboardExampleImage()
//        findCorners(src)
    }

}