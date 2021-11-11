package com.wadiyatalkinabeet.gambit.domain.math.algorithms

import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Point
import kotlin.math.pow

fun List<Point>.sortClockwise(): List<Point> {
    val clockwiseComparator = ClockwiseComparator(
        relativeCentre = Point(
            map { it.x }.average().toFloat(),
            map { it.y }.average().toFloat()
        )
    )
    return sortedWith { point1, point2 ->
        clockwiseComparator(
            point1 = point1, point2 = point2,
        )
    }
}

class ClockwiseComparator(private val relativeCentre: Point) {

    operator fun invoke(point1: Point, point2: Point): Int {
        if (point1.x == point2.x && point1.y == point2.y) return 0
        if (point1.x - relativeCentre.x >= 0 && point2.x - relativeCentre.x < 0){
            return -1
        }
        if (point1.x - relativeCentre.x < 0 && point2.x - relativeCentre.x >= 0){
            return 1
        }
        if (point1.x - relativeCentre.x == 0f && point2.x - relativeCentre.x == 0f){
            if (point1.y - relativeCentre.y >= 0 || point2.y - relativeCentre.y >= 0){
                return if (point1.y > point2.y){
                    -1
                } else {
                    1
                }
            }
            return if (point2.y > point1.y){
                1
            } else {
                -1
            }
        }

        val det: Float = (point1.x - relativeCentre.x) * (point2.y - relativeCentre.y) - (point2.x - relativeCentre.x) * (point1.y - relativeCentre.y)
        //Check the order of this
        if (det < 0) return -1
        if (det > 0) return 1

        return if ((point1.x - relativeCentre.x).pow(2) + (point1.y - relativeCentre.y).pow(2) >
            (point2.x - relativeCentre.x).pow(2) + (point2.y - relativeCentre.y).pow(2)){
            -1
        } else {
            1
        }
    }
}
