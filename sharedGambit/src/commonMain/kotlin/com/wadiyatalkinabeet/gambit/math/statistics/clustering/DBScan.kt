package com.wadiyatalkinabeet.gambit.math.statistics.clustering

import com.wadiyatalkinabeet.gambit.math.datastructures.Point

private enum class Type {
    CORE, REACHABLE, OUTLIER
}

fun DBScan(points: List<Point>, eps: Double = 12.0, minPts: Int = 1): List<Set<Int>> {
    val neighbours = points.indices.map{ it to mutableListOf<Int>() }.toMap()
    val visited = points.indices.map{ it to false}.toMap().toMutableMap()

    points.forEachIndexed { i, point ->
        (i until points.size).filter {
            point.euclideanDistanceTo(points[it]) <= eps
        }.forEach{ j ->
            neighbours[i]!!.add(j)
            neighbours[j]!!.add(i)
        }
    }

    val type = neighbours.map{
        it.key to when (it.value.size) {
            0 -> Type.OUTLIER
            in 1 until minPts -> Type.REACHABLE
            else -> Type.CORE
        }
    }.toMap()

    val clusters = mutableListOf<MutableSet<Int>>()

    // TODO performance Keep track of parent node to avoid running 'step' on it
    fun step(i: Int, cluster: Int? = null) {
        if (visited[i]!!)
            return
        visited[i] = true

        if (type[i] == Type.CORE) {
            val cluster = cluster?.also { clusters[it].add(i) } ?: run {
                clusters.add(mutableSetOf(i))
                clusters.size - 1
            }
            neighbours[i]!!.forEach{ when (type[it]) {
                Type.CORE, Type.REACHABLE -> step(it, cluster)
                Type.OUTLIER -> {}
            }}
            return
        }
        if (type[i] == Type.REACHABLE) {
            clusters[cluster!!].add(i)
            return
        }
    }

    points.indices.filter{ type[it] == Type.CORE }.forEach {
        step(it)
    }

    return clusters
}