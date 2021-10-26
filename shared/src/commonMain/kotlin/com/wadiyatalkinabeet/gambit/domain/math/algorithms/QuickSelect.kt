package com.wadiyatalkinabeet.gambit.domain.math.algorithms

import kotlin.math.floor
import kotlin.random.Random

const val MAX = Int.MAX_VALUE
val rand = Random(0)

fun partition(list:IntArray, left: Int, right:Int, pivotIndex: Int): Int {
    val pivotValue = list[pivotIndex]
    list[pivotIndex] = list[right]
    list[right] = pivotValue
    var storeIndex = left
    for (i in left until right) {
        if (list[i] < pivotValue) {
            val tmp = list[storeIndex]
            list[storeIndex] = list[i]
            list[i] = tmp
            storeIndex++
        }
    }
    val temp = list[right]
    list[right] = list[storeIndex]
    list[storeIndex] = temp
    return storeIndex
}

tailrec fun quickSelect(list: IntArray, left: Int, right: Int, k: Int): Int {
    if (left == right) return list[left]
    var pivotIndex = left + floor((rand.nextInt(MAX) % (right - left + 1)).toDouble()).toInt()
    pivotIndex = partition(list, left, right, pivotIndex)
    if (k == pivotIndex)
        return list[k]
    else if (k < pivotIndex)
        return quickSelect(list, left, pivotIndex - 1, k)
    else
        return quickSelect(list, pivotIndex + 1, right, k)
}