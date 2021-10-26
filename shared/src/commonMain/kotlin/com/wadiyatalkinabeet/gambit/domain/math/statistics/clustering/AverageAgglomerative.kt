package com.wadiyatalkinabeet.gambit.domain.math.statistics.clustering

import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Line

class AverageAgglomerative(val lines: List<Line>, val numClusters: Int = 2) {

    private val clusterDistanceMatrix = mutableMapOf<Set<Int>, Float>()
    private val thetaDistanceMatrix = mutableMapOf<Set<Int>, Float>()

    private val clusters = mutableMapOf<Int, MutableSet<Int>>()

    init {
        require(lines.size >= 2)
        lines.indices.forEach { clusters[it] = mutableSetOf(it) }
        updateDistanceMatrix()
    }

    fun runClustering() : MutableMap<Int, MutableSet<Int>>{
        while (clusters.size > numClusters){
            clusterDistanceMatrix.keys
                .minByOrNull { clusterDistanceMatrix.getValue(it) }
                ?.let {
                clusters[it.last()]?.forEach { clusterMemberIdx -> clusters[it.first()]?.add(clusterMemberIdx) }
                clusters.remove(it.last())
                updateDistanceMatrix(remainingClusterIdx = it.first(), removedClusterIndex = it.last())
            }
        }

        return clusters
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

    private fun averageLinkage(cluster1: MutableSet<Int>, cluster2: MutableSet<Int>): Float {
        return cluster1
            .flatMap { i -> cluster2.map { i to it } }
            .filter { it.first != it.second }
            .map { (thetaIndex1, thetaIndex2) -> thetaDistanceMatrix[setOf(thetaIndex1, thetaIndex2)]
                ?: lines[thetaIndex1].angleTo(lines[thetaIndex2])
                    .also { thetaDistanceMatrix[setOf(thetaIndex1, thetaIndex2)] = it }
            }
            .sum() / (cluster1.size * cluster2.size)
    }
}

class ClusteringException(message: String): ArithmeticException(message)