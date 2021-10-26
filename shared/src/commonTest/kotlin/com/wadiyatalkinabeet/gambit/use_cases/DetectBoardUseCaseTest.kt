package com.wadiyatalkinabeet.gambit.use_cases
//
//import com.wadiyatalkinabeet.gambit.domain.cv.Mat
//import com.wadiyatalkinabeet.gambit.domain.cv.imread
//import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Point
//import kotlinx.serialization.*
//import kotlinx.serialization.json.Json
//import org.junit.jupiter.api.DynamicTest.dynamicTest
//import org.junit.jupiter.api.TestFactory
//import java.io.File
//
//const val EPSILON = 10f
//
//@Serializable
//data class Corners(
//    val bottom_left: List<Float>,
//    val bottom_right: List<Float>,
//    val top_left: List<Float>,
//    val top_right: List<Float>,
//) {
//    val asList = listOf(bottom_left, bottom_right, top_left, top_right)
//}
//
//@Serializable
//data class Metadata(val corners: Corners)
//
//internal class CornerDetectionKtTest{
//
////    init {
////        initOpenCV()
////    }
//
//    val testIds = 1..5
//
//    private fun loadTestImage(id: Int): Pair<Mat, List<Point>> {
//        val img: Mat = imread("src/commonTest/res/tagged_empty_boards/$id.jpg")
//        val metadata = Json.decodeFromString<Metadata>(
//            File("src/commonTest/res/tagged_empty_boards/$id.json").readText()
//        )
//        val corners = metadata.corners.asList.map { Point(it[0], it[1]) }
//
//        return Pair(img, corners)
//    }
//
//    private fun assertApproxEquals(expected: List<Point>, actual: List<Point>) {
//        val distances = expected.indices.map { Float.MAX_VALUE }.toMutableList()
//        val remainingActual = actual.indices.toMutableSet()
//        expected.forEachIndexed { i, target ->
//            var bestActual = -1
//            remainingActual.forEach { j ->
//                val dist = (target - actual[j]).length
//                if (dist < distances[i]) {
//                    distances[i] = dist
//                    bestActual = j
//                }
//            }
//            remainingActual.remove(bestActual)
//        }
//        distances.forEach {
//            assert(it < EPSILON)
//        }
//    }
//
//    @TestFactory
//    fun checkCorrectCornersFound() = testIds.map { id ->
//        dynamicTest("Test image #$id") {
//            val (mat, corners) = loadTestImage(id)
//            val detectBoardUseCase = DetectBoardUseCase()
//            val result = detectBoardUseCase(mat)
//
//            assertApproxEquals(corners, result.data!!.cornerPoints!!)
//        }
//    }
//
//}
