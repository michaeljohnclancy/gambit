package com.wadiyatalkinabeet.gambit.cornerdetection

import com.wadiyatalkinabeet.gambit.FCluster
import com.wadiyatalkinabeet.gambit.angleTo
import com.wadiyatalkinabeet.gambit.median

import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import ru.ifmo.ctddev.igushkin.cg.geometry.Segment
import kotlin.math.*

fun resize(
    img: Mat,
    out: Mat,
    numPixels: Float = 500f.pow(2f),
): Double {
    val w: Double = img.width().toDouble()
    val h: Double = img.height().toDouble()
    val scale: Double = sqrt(numPixels / (w * h))
    Imgproc.resize(img, out, Size(w * scale, h * scale))
    return scale
}

fun imgPreprocess(img: Mat, out: Mat): Double {
    val scale = resize(img, out)
    Imgproc.cvtColor(out, out, Imgproc.COLOR_BGR2GRAY)
    return scale
}

fun applyAutoCanny(
    img: Mat,
    out: Mat,
    sigma: Double = 0.25
) {
    val median = img.median()
    Imgproc.medianBlur(img, out, 5)
    Imgproc.GaussianBlur(out, out, Size(7.0, 7.0), 2.0)
    val lowerThreshold: Double = max(0.0, (1.0 - sigma) * median)
    val upperThreshold: Double = min(255.0, (1.0 + sigma) * median)
    Imgproc.Canny(out, out, lowerThreshold, upperThreshold)
}

private fun applyHoughLines(
    mat: Mat,
    scale: Double = 1.0, // Coordinate scaling
    beta: Double = 2.0
): List<Segment> {
    var lines = Mat()
    Imgproc.HoughLinesP(
        mat, lines, 1.0, PI / 360.0 * beta,
        40, 50.0, 15.0
    )

    val segments: ArrayList<Segment> = arrayListOf()
    for (i in 0 until lines.rows()) {
        val line = lines.get(i, 0)
        segments.add(Segment(
            x0 = (mat.height() - line[1]) / scale,
            y0 = line[0] / scale,
            x1 = (mat.height() - line[3]) / scale,
            y1 = line[2] / scale
        ))
    }
    return segments
}

//TODO Consider appropriate clustering algorithm
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

fun findLines(img: Mat): Pair<List<Segment>, List<Segment>>? {
    val out = Mat()
    val scale = imgPreprocess(img, out)
    applyAutoCanny(out, out)
    val allLines = applyHoughLines(out, scale)
    // Note that which group is 'horizontal' vs 'vertical' is arbitrary here
    val (horizontal, vertical) = cluster(allLines, PI / 16) ?: return null

    return Pair(horizontal, vertical)
}
