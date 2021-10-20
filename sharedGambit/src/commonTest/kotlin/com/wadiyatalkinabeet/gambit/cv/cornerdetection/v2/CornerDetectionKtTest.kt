package com.wadiyatalkinabeet.gambit.cv.cornerdetection.v2

import com.wadiyatalkinabeet.gambit.cv.*
import com.wadiyatalkinabeet.gambit.math.datastructures.Point
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import kotlin.test.assertEquals

const val EPSILON = 10f

@Serializable
data class Corners(
    val bottom_left: List<Float>,
    val bottom_right: List<Float>,
    val top_left: List<Float>,
    val top_right: List<Float>,
) {
    val asList = listOf(bottom_left, bottom_right, top_left, top_right)
}

@Serializable
data class Metadata(val corners: Corners)

internal class CornerDetectionKtTest{

    init {
       initOpenCV()
    }

    val testIds = 1..5

    private fun loadTestImage(id: Int): Pair<Mat, List<Point>> {
        val img: Mat = imread("src/commonTest/res/tagged_empty_boards/$id.jpg")
        val metadata = Json.decodeFromString<Metadata>(
            File("src/commonTest/res/tagged_empty_boards/$id.json").readText()
        )
        val corners = metadata.corners.asList.map { Point(it[0], it[1]) }

        return Pair(img, corners)
    }

    private fun assertApproxEquals(expected: List<Point>, actual: List<Point>) {
        val distances = expected.indices.map { Float.MAX_VALUE }.toMutableList()
        val remainingActual = actual.indices.toMutableSet()
        expected.forEachIndexed { i, target ->
            var bestActual = -1
            remainingActual.forEach { j ->
                val dist = (target - actual[j]).length
                if (dist < distances[i]) {
                    distances[i] = dist
                    bestActual = j
                }
            }
            remainingActual.remove(bestActual)
        }
        distances.forEach {
            assert(it < EPSILON)
        }
    }

    @TestFactory
    fun checkCorrectCornersFound() = testIds.map { id ->
        dynamicTest("Test image #$id") {
            val (img, corners) = loadTestImage(id)
            val imageAnalysis = ImageAnalysisState(img)
            val result = imageAnalysis.findCorners()
                as ImageAnalysisResult.Success
            assertApproxEquals(corners, result.imageAnalysisState.cornerPoints!!)
        }
    }

}