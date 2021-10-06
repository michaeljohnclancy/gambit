package com.wadiyatalkinabeet.gambit.math

import org.jetbrains.kotlinx.multik.api.Multik
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.div
import java.lang.IllegalArgumentException

fun cartesianToHomogenous(cartesianCoordinates: D2Array<Int>): D2Array<Int> {
    return cartesianCoordinates
        .transpose()
        .cat(mk.ones(1, cartesianCoordinates.shape[0]), 0).asD2Array().transpose()
}

fun homogenousToCartesian(homogenousCoordinates: D2Array<Int>): NDArray<Int, D2> {
    return homogenousCoordinates[0.r..homogenousCoordinates.shape[0], 0.r..2] / homogenousCoordinates[0.r..homogenousCoordinates.shape[0], 1.r..2]
}

inline fun <reified T : Number, reified D : Dimension> Multik.ones(vararg dims: Int): NDArray<T, D> {
    val dim = dimensionClassOf<D>(dims.size)
    if (dim.d != dims.size) throw IllegalArgumentException("Dimension doesn't match the size of the shape: dimension (${dim.d}) != ${dims.size} shape size.")
    val dType = DataType.of(T::class)

    val size = dims.fold(1, Int::times)

    val data = initMemoryView(size, dType) { 1.toDType(dType) as T }
    return NDArray(data, shape = dims, dtype = dType, dim = dim)
}

fun <T : Number, D : Dimension> Multik.ones(dims: IntArray, dtype: DataType): NDArray<T, D> {
    // TODO check data type
    val dim = dimensionOf<D>(dims.size)
    if (dim.d != dims.size) throw IllegalArgumentException("Dimension doesn't match the size of the shape: dimension (${dim.d}) != ${dims.size} shape size.")

    val size = dims.fold(1, Int::times)

    @Suppress("UNCHECKED_CAST")
    val data = initMemoryView(size, dtype) { 1.toDType(dtype) as T }
    return NDArray(data, shape = dims, dtype = dtype, dim = dim)
}

fun Number.toDType(dType: DataType) = when (dType.nativeCode) {
    1 -> toByte()
    2 -> toShort()
    3 -> toInt()
    4 -> toLong()
    5 -> toFloat()
    6 -> toDouble()
    else -> throw Exception("Type not defined.")
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun <T: Number> MultiArray<T, DN>.equals(other: MultiArray<T, DN>): Boolean {
    for (index in multiIndices){
        if (this[index] != other[index]){
            return false
        }
    }
    return true
}
