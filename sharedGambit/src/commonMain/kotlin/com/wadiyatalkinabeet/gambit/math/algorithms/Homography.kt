package com.wadiyatalkinabeet.gambit.math.algorithms


import com.wadiyatalkinabeet.gambit.cv.*

fun computeHomography(intersectionPoints: Array<Array<Point?>>, rowIndex1: Int, rowIndex2: Int, colIndex1: Int, colIndex2: Int): Mat {

    val topLeft = intersectionPoints[rowIndex1][colIndex1]
    val topRight = intersectionPoints[rowIndex1][colIndex2]
    val bottomRight = intersectionPoints[rowIndex2][colIndex2]
    val bottomLeft = intersectionPoints[rowIndex2][colIndex1]

    if (topLeft == null || topRight == null || bottomLeft == null || bottomRight == null){
        throw NoSuchElementException("No intersection point at position")
    }

    val points = MatOfPoint2f(topLeft, topRight, bottomRight, bottomLeft)

    val homographyDestinationPoints = MatOfPoint2f(Point(0.0, 0.0), Point(1.0, 0.0), Point(1.0, 1.0), Point(0.0, 1.0))

    return findHomography(points, homographyDestinationPoints)
}
