package com.wadiyatalkinabeet.gambit.math.statistics.clustering

import com.wadiyatalkinabeet.gambit.math.datastructures.Line

class AverageAgglomerative(val lines: List<Line>, val numClusters: Int = 2) {

    private val clusterDistanceMatrix = mutableMapOf<Set<Int>, Double>()
    private val thetaDistanceMatrix = mutableMapOf<Set<Int>, Double>()

    val clusters = mutableMapOf<Int, MutableSet<Int>>()

    init {
        lines.indices.forEach { clusters[it] = mutableSetOf(it) }
        updateDistanceMatrix()
    }

    fun run() {
        while (clusters.size > numClusters){
            clusterDistanceMatrix.keys
                .minByOrNull { clusterDistanceMatrix.getValue(it) }
                ?.let {
                clusters[it.last()]?.forEach { clusterMemberIdx -> clusters[it.first()]?.add(clusterMemberIdx) }

                clusters.remove(it.last())

                updateDistanceMatrix(remainingClusterIdx = it.first(), removedClusterIndex = it.last())
            }
        }
    }

    private fun updateDistanceMatrix(remainingClusterIdx: Int, removedClusterIndex: Int? = null){
        removedClusterIndex?.let {
            clusterDistanceMatrix
                .filter { it.key.contains(removedClusterIndex) }
                .forEach { clusterDistanceMatrix.remove(it.key) }
        }

        for (cluster2Index in clusters.keys) {
            if (remainingClusterIdx  == cluster2Index) continue
            clusterDistanceMatrix[setOf(remainingClusterIdx, cluster2Index)] = averageLinkage(
                cluster1 = clusters.getValue(remainingClusterIdx), cluster2 = clusters.getValue(cluster2Index))
        }
    }

    private fun updateDistanceMatrix(){
        for (cluster1Index in clusters.keys){
            updateDistanceMatrix(cluster1Index)
        }
    }

    private fun averageLinkage(cluster1: MutableSet<Int>, cluster2: MutableSet<Int>): Double {
        return cluster1
            .flatMap { i -> cluster2.map { i to it } }
            .filter { it.first != it.second }
            .map { (thetaIndex1, thetaIndex2) -> thetaDistanceMatrix[setOf(thetaIndex1, thetaIndex2)]
                ?: lines[thetaIndex1].angleTo(lines[thetaIndex2])
                    .also { thetaDistanceMatrix[setOf(thetaIndex1, thetaIndex2)] = it }
            }
            .sumOf { it } / (cluster1.size * cluster2.size)

    }
}