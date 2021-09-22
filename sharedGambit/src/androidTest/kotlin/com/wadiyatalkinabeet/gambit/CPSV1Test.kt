package com.wadiyatalkinabeet.gambit

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.wadiyatalkinabeet.gambit.ml.NeuralLAPS

import kotlin.test.Test

internal class CPSV1Test {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun runChessboardPositionSearch() {
        val testImage = loadChessboardExampleImage()
        val model = NeuralLAPS.newInstance(context)

        val bitmap = (testImage)

//        bitmap.toDisk("src/commonTest/res/cps/cpstest.png")

    }

}