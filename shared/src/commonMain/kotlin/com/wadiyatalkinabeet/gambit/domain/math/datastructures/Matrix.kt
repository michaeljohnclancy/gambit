package com.wadiyatalkinabeet.gambit.domain.math.datastructures

import com.wadiyatalkinabeet.gambit.domain.cv.CV_32FC1
import com.wadiyatalkinabeet.gambit.domain.cv.Mat


typealias Matrix2D = Array<FloatArray>

fun Matrix2D.inverse(): Matrix2D {
    val len = this.size
    require(this.all { it.size == len }) { "Not a square matrix" }
    val aug = Array(len) { FloatArray(2 * len) }
    for (i in 0 until len) {
        for (j in 0 until len) aug[i][j] = this[i][j]
        // augment by identity matrix to right
        aug[i][i + len] = 1f
    }
    aug.toReducedRowEchelonForm()
    val inv = Array(len) { FloatArray(len) }
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

        while (this[i][lead] == 0f) {
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

        if (this[r][lead] != 0f) {
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
            if (this[r][c] == -0f) this[r][c] = 0f // get rid of negative zeros
            print("${this[r][c]}")
        }
        println()
    }

    println()
}

fun Mat.toMatrix(): Matrix2D {
    val transformationMatrix = Matrix2D(this.rows()) { FloatArray(this.cols()) }
    for (i in 0 until this.rows()){
        for (j in 0 until this.cols()){
            transformationMatrix[i][j] = this[i, j][0]
        }
    }
    return transformationMatrix
}

fun Matrix2D.toMat(): Mat {
    val mat = Mat(this.size, this[0].size, CV_32FC1)
    for (i in this.indices) {
        for (j in this[i].indices) {
            mat[i, j] = floatArrayOf(this[i][j])
        }
    }
    return mat
}