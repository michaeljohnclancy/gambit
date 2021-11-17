package com.wadiyatalkinabeet.gambit.domain.cv.occupancyclassification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dev.icerock.moko.tensorflow.Interpreter
import dev.icerock.moko.tensorflow.InterpreterOptions
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.ByteArrayOutputStream


@RunWith(RobolectricTestRunner::class)
internal class OccupancyClassifierTest {
    private lateinit var interpreter: Interpreter

    @Test
    fun testModel() {
        val appContext = RuntimeEnvironment.systemContext
        interpreter = Interpreter(
            ResHolder.getModelFile(),
            InterpreterOptions(2, useNNAPI = true),
            context = appContext
        )
        val classifier = OccupancyClassifier(interpreter)
        classifier.classify(bitmapFromDisk())
    }

    private fun bitmapFromDisk(): ByteArray {
        val bitmap = BitmapFactory.decodeFile("/Users/mclancy/Documents/gambit/shared/src/commonMain/resources/MR/images/occupancyclassification/emptysquare@1x.png")
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        return stream.toByteArray()
    }
}
