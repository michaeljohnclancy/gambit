package com.wadiyatalkinabeet.gambit.math.statistics.clustering

import com.wadiyatalkinabeet.gambit.math.datastructures.Line

class AverageAgglomerative(val lines: List<Line>, val numClusters: Int = 2) {

    private val distanceMatrix = mutableMapOf<Pair<Int, Int>, Double>()
//    private val clusters = lines.indices.map { Pair(it, it) }
    val clusters = mutableMapOf<Int, MutableSet<Int>>()
//
//    private val disjointSet = DisjointSet(size = lines.size)
//
    init {
        lines.indices.forEach { clusters[it] = mutableSetOf(it) }
        recalculateDistanceMatrix()
    }

    fun run(): MutableMap<Int, MutableSet<Int>> {
        while (clusters.size > numClusters){
            distanceMatrix
            .minByOrNull { it.value }?.key
            ?.let {
                clusters[it.second]?.forEach { clusterMemberIdx -> clusters[it.first]?.add(clusterMemberIdx) }
                clusters.remove(it.second)
                distanceMatrix.remove(it)
                //Bit fucked
            }
            recalculateDistanceMatrix()
        }
        return clusters
    }

    private fun recalculateDistanceMatrix(){
//        clusters.keys.flatMap { i -> cluster2.map { i to it }
//        clusters.keys.indices.flatMap { i -> clusters.keys.indices.map { it to i-1 } }

        for (i in clusters.keys.indices){
            for (j in 0..i){
                if (i == j) continue
                distanceMatrix[Pair(clusters.keys.elementAt(i), clusters.keys.elementAt(j))] = averageLinkage(clusters.values.elementAt(i), clusters.values.elementAt(j))
            }
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