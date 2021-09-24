package com.wadiyatalkinabeet.gambit.math.algorithms

import java.lang.RuntimeException

class NthElement {

    private fun nthElementHelper1(arr: DoubleArray, beg: Int, end: Int) {
        for (i in beg + 1 until end) {
            for (j in i downTo beg + 1) {
                if (arr[j - 1] < arr[j]) break
                val t = arr[j]
                arr[j] = arr[j - 1]
                arr[j - 1] = t
            }
        }
    }

    private fun nthElementHelper0(arr: DoubleArray, beg: Int, end: Int, index: Int) {
        var beg = beg
        var end = end
        if (beg + 4 >= end) {
            nthElementHelper1(arr, beg, end)
            return
        }
        val initial_beg = beg
        val initial_end = end

        // Pick a pivot (using the median of 3 technique)
        val pivA = arr[beg]
        val pivB = arr[(beg + end) / 2]
        val pivC = arr[end - 1]
        val pivot: Double = if (pivA < pivB) {
            if (pivB < pivC) pivB else if (pivA < pivC) pivC else pivA
        } else {
            if (pivA < pivC) pivA else if (pivB < pivC) pivC else pivB
        }

        // Divide the values about the pivot
        while (true) {
            while (beg + 1 < end && arr[beg] < pivot) beg++
            while (end > beg + 1 && arr[end - 1] > pivot) end--
            if (beg + 1 >= end) break

            // Swap values
            val t = arr[beg]
            arr[beg] = arr[end - 1]
            arr[end - 1] = t
            beg++
            end--
        }
        if (arr[beg] < pivot) beg++

        // Recurse
        if (beg == initial_beg || end == initial_end) throw RuntimeException("No progress. Bad pivot")
        if (index < beg) // This is where we diverge from QuickSort. We only recurse on one of the two sides. This is what makes nth_element fast.
            nthElementHelper0(arr, initial_beg, beg, index) else nthElementHelper0(
            arr,
            beg,
            initial_end,
            index
        )
    }

    fun run(arr: DoubleArray, index: Int): Double {
        nthElementHelper0(arr, 0, arr.size, index)
        return arr[index]
    }
}