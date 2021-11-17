package com.wadiyatalkinabeet.gambit.domain.cv.occupancyclassification

import com.wadiyatalkinabeet.gambit.MR
import com.wadiyatalkinabeet.gambit.Resource
import com.wadiyatalkinabeet.gambit.domain.cv.Mat
import dev.icerock.moko.resources.FileResource
import dev.icerock.moko.tensorflow.Interpreter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

class OccupancyClassifier(private val interpreter: Interpreter) {
    private val numClasses = 2

    fun classify(inputData: Any) {
        val inputShape = interpreter.getInputTensor(0).shape
        val inputSize = inputShape[1]

        val result = Array(1) { FloatArray(numClasses) }
        interpreter.run(listOf(inputData), mapOf(Pair(0, result)))
    }
}

object ResHolder {
    fun getModelFile(): FileResource {
        return MR.files.OccupancyClassifier
    }
}

fun Flow<Mat>.positionOccupied(): Flow<Resource<Boolean>> = transform {

}
