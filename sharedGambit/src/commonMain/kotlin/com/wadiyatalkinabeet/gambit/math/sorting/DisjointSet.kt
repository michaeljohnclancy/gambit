package com.wadiyatalkinabeet.gambit.math.sorting

class DisjointSet(val size: Int) {
    private val rank = ByteArray(size)
    val parent = IntArray(size)
    var count = size

        private set
    init {
        for (i in parent.indices) {
            parent[i] = i
        }
    }
    fun connected(v: Int, w: Int): Boolean {
        return find(v) == find(w)
    }
    fun find(v: Int): Int {
        var v = v
        while (parent[v] != v) {
            parent[v] = parent[parent[v]]
            v = parent[v]
        }
        return v
    }
    fun union(v: Int, w: Int) {
        val rootV = find(v)
        val rootW = find(w)
        if (rootV == rootW) return
        if (rank[rootV] > rank[rootW]) {
            parent[rootW] = rootV
        } else if (rank[rootW] > rank[rootV]) {
            parent[rootV] = rootW
        } else {
            parent[rootV] = rootW
            rank[rootW]++
        }
        count--
    }

    fun <T: Any> populateDisjointSet(objects: List<T>, subsetIndices: List<Int>?, comparator: (T, T) -> Boolean) {
        (subsetIndices?.map{it+1}?:IntRange(1, objects.size-1))
            .flatMap { i -> objects.indices.map { i-1 to it } }
            .filter { comparator(objects[it.first], objects[it.second]) }
            .onEach {
                this.union(
                    it.first,
                    it.second
                )
            }
    }

}