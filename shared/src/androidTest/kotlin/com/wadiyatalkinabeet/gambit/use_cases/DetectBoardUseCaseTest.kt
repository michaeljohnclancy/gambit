package com.wadiyatalkinabeet.gambit.use_cases

import com.wadiyatalkinabeet.gambit.Resource
import com.wadiyatalkinabeet.gambit.domain.cv.Mat
import com.wadiyatalkinabeet.gambit.domain.cv.imread
import com.wadiyatalkinabeet.gambit.domain.cv.initOpenCV
import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Point
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File

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

class DetectBoardUseCaseTest{

    init {
        initOpenCV()
    }

    private val testIds = 1..10

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
                runBlocking {
                    val expectedCorners = loadExpectedCorners(listOf(id))[0]
                    val actualCorners = fakeMatFlow(listOf(id))
                        .detectBoardUseCase()
                        .filter { it is Resource.Success }
                        .first()
                        .data!!
                        .cornerPoints!!
                    assertApproxEquals(expectedCorners, actualCorners)
                }
            }
        }

    private fun fakeMatFlow(imageIDs: List<Int>): Flow<Mat> = flow {
        for(id in imageIDs){
            emit(imread("src/commonTest/res/tagged_empty_boards/$id.jpg"))
        }
    }

    private fun loadExpectedCorners(imageIDs: List<Int>): List<List<Point>> =
        imageIDs
            .map { id ->
                Json.decodeFromString<Metadata>(
                    File("src/commonTest/res/tagged_empty_boards/$id.json").readText()
                ).corners.asList.map { Point(it[0], it[1]) }
            }
}
