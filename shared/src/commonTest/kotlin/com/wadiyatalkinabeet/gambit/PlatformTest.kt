package com.wadiyatalkinabeet.gambit

import com.wadiyatalkinabeet.gambit.domain.cv.loadChessboardExampleImage
import com.wadiyatalkinabeet.gambit.domain.cv.processImage
import kotlin.test.Test

class PlatformTest {

    @Test
    fun platformTest(){
        val matIn = loadChessboardExampleImage()
        processImage(matIn)
    }

}