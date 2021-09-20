package com.wadiyatalkinabeet.gambit

import android.os.Build
import org.opencv.android.OpenCVLoader

actual fun getNameOfPlatform(): String {
    return "Android ${Build.VERSION.RELEASE}"
}

actual fun initOpenCV() {
    OpenCVLoader.initDebug()
}

