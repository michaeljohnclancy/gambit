package com.wadiyatalkinabeet.gambit

import android.graphics.Bitmap
import com.wadiyatalkinabeet.gambit.ml.NeuralLAPS
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import ru.ifmo.ctddev.igushkin.cg.geometry.Point
import ru.ifmo.ctddev.igushkin.cg.geometry.Segment
import kotlin.math.pow
import kotlin.math.sqrt

class CPS(private val neuralLAPS: NeuralLAPS) {

    fun runLAPS(mat: Mat): List<Point> {
        var (tmpMat, newSize, scale) = resize(mat = mat)
        val segments: List<Segment> = SLID().analyze(tmpMat)
        return LAPS(neuralLAPS).analyze(tmpMat, segments = segments).map { Point(it.x / scale, it.y / scale) }
    }

    fun runChessboardPositionSearch(mat: Mat): Bitmap {

        mat.applyPoints(runLAPS(mat))

        var bitmap =
            Bitmap.createBitmap(
                mat.width(), mat.height(),
                Bitmap.Config.ARGB_8888
            )

        Utils.matToBitmap(mat, bitmap);

        return bitmap
    }

    companion object {
        init {
            OpenCVLoader.initDebug()
        }

        fun resize(
            mat: Mat,
            height: Double = 500.0
        ): Triple<Mat, Size, Double> {

            var tmpMat = mat.clone()

            val numPixels: Double = height.pow(2.0)

            var w: Double = tmpMat.width().toDouble()
            var h: Double = tmpMat.height().toDouble()
            val scale: Double = sqrt(numPixels / (w * h))
            w *= scale
            h *= scale
            val scaleSize = Size(w, h)
            Imgproc.resize(tmpMat, tmpMat, scaleSize)

            return Triple(tmpMat, tmpMat.size(), scale)
        }
    }
}
