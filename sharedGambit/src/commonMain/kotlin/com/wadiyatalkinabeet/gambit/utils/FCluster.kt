package com.wadiyatalkinabeet.gambit.utils

import ru.ifmo.ctddev.igushkin.cg.geometry.Point
import ru.ifmo.ctddev.igushkin.cg.geometry.distance

fun fcluster(points: List<Point>, maxDist: Double): List<Point> {

    return points
}

typealias Comparator = (Int, Int) -> Boolean

class FCluster(private var count: Int, private var comparator: Comparator) {

    private var itemClusters: Array<MutableSet<Int>?> = arrayOfNulls<MutableSet<Int>?>(count)
    private var clusters: MutableList<Set<Int>?> = ArrayList()

    companion object{
        fun apply(count: Int, comparator: Comparator): List<Set<Int?>?> {
            val clustering = FCluster(count, comparator)
            clustering.run()
            return clustering.getClusters()
        }
    }

    private fun run() {
        buildClusters()
        makeUnique()
    }

    private fun getClusters(): List<Set<Int>?> {
        return clusters
    }

    private fun buildClusters() {
        for (item in 0 until count) {
            if (itemClusters[item] == null) {
                itemClusters[item] = HashSet()
                itemClusters[item]?.add(item)
            }
            for (other in item + 1 until count) {
                if (comparator(item, other)) {
                    joinClusters(item, other)
                }
            }
        }
    }

    private fun joinClusters(first: Int, second: Int) {
        val cluster = itemClusters[first]
        if (itemClusters[second] == null) {
            cluster!!.add(second)
            itemClusters[second] = cluster
        } else {
            val secondCluster: Set<Int>? = itemClusters[second]
            cluster!!.addAll(secondCluster!!)
            for (c in secondCluster) {
                itemClusters[c] = cluster
            }
        }
    }

    private fun makeUnique() {
        for (s in itemClusters) {
            var found = false
            for (c in clusters) {
                if (c === s) {
                    found = true
                    break
                }
            }
            if (!found) {
                clusters.add(s)
            }
        }
    }
}