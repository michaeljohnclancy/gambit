package com.wadiyatalkinabeet.gambit.math.algorithms


fun meshGrid(x: FloatArray, y: FloatArray, indexing: MeshgridIndex = MeshgridIndex.XY): Pair<Array<FloatArray>, Array<FloatArray>> {

    val (sizeX, sizeY) = Pair(x.size, y.size)

    return when (indexing) {
        MeshgridIndex.XY -> {
            val X = Array(sizeY) {
                FloatArray(
                    sizeX
                )
            }
            val Y = Array(sizeY) {
                FloatArray(
                    sizeX
                )
            }

            for (i in 0 until sizeX) {
                for (j in 0 until sizeY) {
                    X[j][i] = x[i]
                    Y[j][i] = y[j]
                }
            }

            Pair(X, Y)


        }
        MeshgridIndex.IJ -> {

            val X = Array(sizeX) {
                FloatArray(
                    sizeY
                )
            }
            val Y = Array(sizeX) {
                FloatArray(
                    sizeY
                )
            }

            for (i in 0 until sizeX) {
                for (j in 0 until sizeY) {
                    X[i][j] = x[i]
                    Y[i][j] = y[j]
                }
            }
            Pair(X, Y)

        }
    }
}
enum class MeshgridIndex{ XY, IJ }
