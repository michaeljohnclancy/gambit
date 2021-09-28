package com.wadiyatalkinabeet.gambit.math.datastructures

import com.wadiyatalkinabeet.gambit.cv.CV_64F
import com.wadiyatalkinabeet.gambit.cv.CV_8UC1
import com.wadiyatalkinabeet.gambit.cv.Mat


typealias Matrix2D = Array<DoubleArray>

fun Matrix2D.inverse(): Matrix2D {
    val len = this.size
    require(this.all { it.size == len }) { "Not a square matrix" }
    val aug = Array(len) { DoubleArray(2 * len) }
    for (i in 0 until len) {
        for (j in 0 until len) aug[i][j] = this[i][j]
        // augment by identity matrix to right
        aug[i][i + len] = 1.0
    }
    aug.toReducedRowEchelonForm()
    val inv = Array(len) { DoubleArray(len) }
    // remove identity matrix to left
    for (i in 0 until len) {
        for (j in len until 2 * len) inv[i][j - len] = aug[i][j]
    }
    return inv
}

fun Matrix2D.toReducedRowEchelonForm() {
    var lead = 0
    val rowCount = this.size
    val colCount = this[0].size
    for (r in 0 until rowCount) {
        if (colCount <= lead) return
        var i = r

        while (this[i][lead] == 0.0) {
            i++
            if (rowCount == i) {
                i = r
                lead++
                if (colCount == lead) return
            }
        }

        val temp = this[i]
        this[i] = this[r]
        this[r] = temp

        if (this[r][lead] != 0.0) {
            val div = this[r][lead]
            for (j in 0 until colCount) this[r][j] /= div
        }

        for (k in 0 until rowCount) {
            if (k != r) {
                val mult = this[k][lead]
                for (j in 0 until colCount) this[k][j] -= this[r][j] * mult
            }
        }

        lead++
    }
}

fun Matrix2D.printf(title: String) {
    println(title)
    val rowCount = this.size
    val colCount = this[0].size

    for (r in 0 until rowCount) {
        for (c in 0 until colCount) {
            if (this[r][c] == -0.0) this[r][c] = 0.0  // get rid of negative zeros
            print("${"% 10.6f".format(this[r][c])}  ")
        }
        println()
    }

    println()
}

fun Mat.toMatrix(): Matrix2D{
    val transformationMatrix = Matrix2D(this.rows()) { DoubleArray(this.cols()) }
    for (i in 0 until this.rows()){
        for (j in 0 until this.cols()){
            transformationMatrix[i][j] = this.get(i, j)[0]
        }
    }
    return transformationMatrix
}

fun Matrix2D.toMat(): Mat {
    var mat = Mat(this.size, this[0].size, CV_64F)
    for (i in this.indices) {
        for (j in this[i].indices) {
            mat.put(i, j, this[i][j])
        }
    }
    return mat
}