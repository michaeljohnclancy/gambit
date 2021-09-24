package com.wadiyatalkinabeet.gambit.math.statistics.clustering

import com.wadiyatalkinabeet.gambit.math.datastructures.Line
import ru.ifmo.ctddev.igushkin.cg.geometry.distance

class AverageAgglomerative(val lines: List<Line>, val numClusters: Int = 2) {

    private val distanceMatrix = mutableMapOf<Pair<Int, Int>, Double>()
//    private val clusters = lines.indices.map { Pair(it, it) }
    val clusters = mutableMapOf<Int, MutableSet<Int>>()
//
//    private val disjointSet = DisjointSet(size = lines.size)
//
    init {
        lines.indices.forEach { clusters[it] = mutableSetOf(it) }
        updateDistanceMatrix()
    }

    fun run() {
        while (clusters.size > numClusters){
            clusters.keys.flatMap { i -> clusters.keys.map { i to it }}
                .filter { it.first != it.second }
                .minByOrNull { distanceMatrix[Pair(it.first, it.second)]!! }
            ?.let {
                clusters[it.second]?.forEach { clusterMemberIdx -> clusters[it.first]?.add(clusterMemberIdx) }
                clusters.remove(it.second)

//                distanceMatrix.remove(it)
                updateDistanceMatrix(cluster1Index = it.first)
            }
        }
    }

    private fun updateDistanceMatrix(cluster1Index: Int){

        for (idx in clusters.keys) {
//            distanceMatrix.remove(Pair(idx, cluster2Index))
//            distanceMatrix.remove(Pair(cluster2Index, idx))
            if (cluster1Index == idx) continue
            distanceMatrix[Pair(cluster1Index, idx)] = averageLinkage(
                cluster1 = clusters.getValue(cluster1Index), cluster2 = clusters.getValue(idx))
        }
    }

    private fun updateDistanceMatrix(){
        for (cluster1Index in clusters.keys){
            updateDistanceMatrix(cluster1Index)
        } }

//            clusters.flatMap { cluster1 -> clusters.map { cluster2 -> cluster1 to cluster2 }}
//            .forEach { distanceMatrix[Pair(it.first.key, it.second.key)] = averageLinkage(it.first.value, it.second.value)}
    private fun averageLinkage(cluster1: MutableSet<Int>, cluster2: MutableSet<Int>): Double {
        return cluster1
            .flatMap { i -> cluster2.map { i to it } }
            .map { lines[it.first].angleTo(lines[it.second]) }
            .sumOf { it } / (cluster1.size * cluster2.size)

    }
}