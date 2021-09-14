package com.wadiyatalkinabeet.gambit

import android.graphics.Bitmap
import com.google.android.play.core.tasks.Task
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import ru.ifmo.ctddev.igushkin.cg.geometry.Segment
import kotlin.math.pow
import kotlin.math.sqrt

fun runChessboardPositionSearch(mat: Mat): Bitmap{
    OpenCVLoader.initDebug()

    var (mat, newSize, scale)   = resize(mat = mat)
    //Run SLID

    val slid = SLID()
    val segments: List<Segment> = slid.analyze(mat)

    //TEMPORARY
    mat.applyLines(segments)
    //Then LAPS

    //THEN CPS

    var bitmap =
        Bitmap.createBitmap(
            mat.width(), mat.height(),
            Bitmap.Config.ARGB_8888
        )

    Utils.matToBitmap(mat, bitmap);

    return bitmap
}

fun resize(
    mat: Mat,
    height: Double = 500.0
): Triple<Mat, Size, Double> {

    val numPixels: Double = height.pow(2.0)

    var w: Double = mat.width().toDouble()
    var h: Double = mat.height().toDouble()
    val scale: Double = sqrt(numPixels / (w * h))
    w *= scale
    h *= scale
    val scaleSize = Size(w, h)
    Imgproc.resize(mat, mat, scaleSize)

    return Triple(mat, mat.size(), scale)
}
