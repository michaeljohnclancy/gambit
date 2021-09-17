package com.wadiyatalkinabeet.gambit

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.wadiyatalkinabeet.gambit.ml.NeuralLAPS

import org.junit.jupiter.api.Test

internal class CPSTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun runChessboardPositionSearch() {
        val testImage = loadChessboardExampleImage()
        val model = NeuralLAPS.newInstance(context)

        val bitmap = CPS(model).runChessboardPositionSearch(testImage)

        bitmap.toDisk("src/commonTest/res/cps/cpstest.png")

    }

}