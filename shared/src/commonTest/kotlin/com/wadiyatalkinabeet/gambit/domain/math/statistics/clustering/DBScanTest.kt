package com.wadiyatalkinabeet.gambit.domain.math.statistics.clustering
//
//import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Point
//import org.junit.jupiter.api.DynamicTest
//import org.junit.jupiter.api.TestFactory
//import kotlin.math.PI
//import kotlin.math.cos
//import kotlin.math.sin
//import kotlin.test.assertEquals
//import kotlin.test.assertTrue
//
//class DBScanTest {
//
//    /*
//    * Fixtures take the form List<List<Point>>. When merged and shuffled into a single list of
//    * randomized points, DBScan should return the original groupings (in any order).
//    */
//
//    data class ExampleClustering(
//        val description: String,
//        val clusters: List<List<Point>>,
//        val eps: Float = 12f,
//        val minPts: Int = 1
//    )
//
//    private val exampleClusterings: List<ExampleClustering> = listOf(
//        ExampleClustering("independent points",
//            (0 until 100).map { listOf(Point(10f * it, 0f)) },
//            eps = 9.9f
//        ),
//        ExampleClustering("connected points",
//            listOf((0 until 100).map { Point(10f * it, 0f) }),
//            eps = 10.1f
//        ),
//        ExampleClustering("independent concentric circles",
//            (0 until 4).map { genCircle(Point(0f, 0f), 25f + 50f * it) },
//            eps = 12.0f
//        ),
//        ExampleClustering("connected concentric circles",
//            listOf((0 until 12).flatMap { genCircle(Point(0f, 0f), 25f + 5f * it) })
//        ),
//        ExampleClustering("independent but reachable crosses",
//            (0 until 128).map { genCross(Point(3f * it, 0f), 1f) },
//            eps = 1.001f, minPts = 4
//        )
//    )
//
//    /*
//    * Tests
//    */
//
//    @TestFactory
//    fun checkClusteredCorrectly() = exampleClusterings.map { clustering ->
//        DynamicTest.dynamicTest(
//            "Correct clusters found for ${clustering.description}"
//        ) {
//            val trueClusters = clustering.clusters
//            // Shuffle points (but keep track of where they came from)
//            val randomOrdering = mergeAndShuffle(trueClusters)
//            val allPoints = randomOrdering.map { (spiralIndex, pointIndex) ->
//                trueClusters[spiralIndex][pointIndex]
//            }
//
//            // Perform clustering on shuffled points
//            val guessClusters = DBScan(allPoints, clustering.eps, clustering.minPts)
//
//            // Assert correct number of clusters
//            assertEquals(trueClusters.size, guessClusters.size)
//
//            val clustersSeen: MutableSet<Int> = mutableSetOf()
//            guessClusters.forEach { indices ->
//                // Determine expected cluster index from the first point's true cluster index
//                val clusterIndex = randomOrdering[indices.first()].first
//                // Assert that this is an unseen cluster index
//                assertTrue(clustersSeen.add(clusterIndex))
//                // Assert correct number of points in cluster
//                assertEquals(trueClusters[clusterIndex].size, indices.size)
//                indices.drop(1).forEach {
//                    // Assert each point belongs in this cluster
//                    assertEquals(randomOrdering[it].first, clusterIndex)
//                }
//            }
//            // Assert all clusters were observed
//            assertEquals(trueClusters.size, clustersSeen.size)
//        }
//    }
//
//    companion object {
//        // Generate a random ordering of points, returning in the form List(Pair(spiralIndex, pointIndex))
//        fun mergeAndShuffle(clusters: List<List<Point>>): List<Pair<Int, Int>> {
//            return clusters.flatMapIndexed{ spiralIndex, spiral ->
//                spiral.indices.map{ pointIndex ->
//                    Pair(spiralIndex, pointIndex)
//                }
//            }.shuffled()
//        }
//
//        /*
//        * Helper functions to generate point structures
//        */
//
//        fun genCircle(
//            center: Point,
//            r: Float = 50f,
//            n: Int = 100
//        ): List<Point> = (0 until n).map {
//            val theta = it * 2*PI.toFloat() / n
//            center + Point(r * cos(theta), r * sin(theta))
//        }
//
//        fun genCross(
//            center: Point,
//            r: Float = 1f
//        ): List<Point> = listOf(
//            center,
//            center + Point(-r, 0f),
//            center + Point(r, 0f),
//            center + Point(0f, -r),
//            center + Point(0f, r)
//        )
//
//    }
//
//}