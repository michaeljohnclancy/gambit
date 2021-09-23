package com.wadiyatalkinabeet.gambit.cv.cornerdetection.v2

import com.wadiyatalkinabeet.gambit.cv.*
import com.wadiyatalkinabeet.gambit.math.geometry.Segment
import com.wadiyatalkinabeet.gambit.math.statistics.clustering.FCluster
import kotlin.math.*

fun resize(
    img: Mat,
    out: Mat,
    numPixels: Float = 500f.pow(2f),
): Double {
    val w: Double = img.width().toDouble()
    val h: Double = img.height().toDouble()
    val scale: Double = sqrt(numPixels / (w * h))
    resize(img, out, Size(w * scale, h * scale))
    return scale
}

private fun applyHoughLines(
    mat: Mat,
    scale: Double = 1.0, // Coordinate scaling
    beta: Double = 2.0
): List<Segment> {
    var lines = Mat()
    houghLinesP(
        mat, lines, 1.0, PI / 360.0 * beta,
        40, 50.0, 15.0
    )

    val segments: ArrayList<Segment> = arrayListOf()
    for (i in 0 until lines.rows()) {
        lines[i, 0]?.let {
            segments.add(Segment(
                x0 = (mat.height() - it[1]) / scale,
                y0 = it[0] / scale,
                x1 = (mat.height() - it[3]) / scale,
                y1 = it[2] / scale
            ))
        }

    }
    return segments
}

////TODO Consider appropriate clustering algorithm
fun cluster(lines: List<Segment>, maxAngle: Double = PI/180): Pair<List<Segment>, List<Segment>>? {
    val a = FCluster.apply(lines.size) { index1, index2 ->
        lines[index1].angleTo(lines[index2]) <= maxAngle
    }
    val allClusters =  a.map { cluster ->
        cluster!!.map {
            it!!.let { lines[it] }
        }
    }
    if (allClusters.size < 2)
        return null

    return allClusters.sortedBy{-it.size}.take(2).let{Pair(it[0], it[1])}
}

fun imgPreprocess(img: Mat, out: Mat): Double {
    val scale = resize(img, out)
    cvtColor(out, out, COLOR_BGR2GRAY)
    return scale
}

fun findLines(img: Mat): Pair<List<Segment>, List<Segment>>? {
    val out = Mat()
    val scale = imgPreprocess(img, out)
    autoCanny(out, out)
    val allLines = applyHoughLines(out, scale)
    // Note that which group is 'horizontal' vs 'vertical' is arbitrary here
    val (horizontal, vertical) = cluster(allLines, PI / 16) ?: return null

    return Pair(horizontal, vertical)
}


