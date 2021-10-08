package com.wadiyatalkinabeet.gambit.android

import android.annotation.SuppressLint
import androidx.camera.view.PreviewView
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.github.skgmn.cameraxx.CameraPreview
import com.github.skgmn.startactivityx.PermissionStatus
import com.wadiyatalkinabeet.gambit.CameraPreviewViewModel
import com.wadiyatalkinabeet.gambit.cv.ImageAnalysisResult
import com.wadiyatalkinabeet.gambit.cv.Point
import com.wadiyatalkinabeet.gambit.cv.cvToScreenCoords
import com.wadiyatalkinabeet.gambit.math.datastructures.Segment
import com.wadiyatalkinabeet.gambit.math.datastructures.Line
import kotlinx.coroutines.flow.Flow


@SuppressLint("CoroutineCreationDuringComposition")
@ExperimentalAnimationApi
@Composable
fun MainScreen(
    viewModel: CameraPreviewViewModel,
    permissionStatusFlow: Flow<PermissionStatus>,
    onRequestCameraPermission: () -> Unit
){
    val permissionStatus by permissionStatusFlow.collectAsState(initial = null)
    val permissionInitiallyRequested by viewModel
        .permissionsInitiallyRequestedState.collectAsState(initial = false)

    if (permissionStatus?.granted == true){
        CameraLayer(viewModel = viewModel)
        ImageAnalysisOverlay(viewModel = viewModel)
    }
    if (permissionInitiallyRequested && permissionStatus?.denied == true) {
        PermissionLayer(onRequestCameraPermission)
    }
}

@Composable
private fun CameraLayer(
    viewModel: CameraPreviewViewModel
){
    val preview by remember { mutableStateOf(viewModel.preview) }
    val imageAnalysis by viewModel.imageAnalysisUseCaseState.collectAsState()

    CameraPreview(
        modifier = Modifier.fillMaxSize(),
        preview = preview,
        scaleType = PreviewView.ScaleType.FIT_START,
        imageAnalysis = imageAnalysis
    )
}

//@Composable
//fun LatticeOverlayLayer(
//    viewModel: CameraPreviewViewModel
//) {
//    val latticeLines by viewModel
//        .getLatticeLines().collectAsState(initial = null)
//
//    val imageAnalysisResolution by viewModel.imageAnalysisResolution.collectAsState()
//
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            val screenSize = Pair(size.width.toInt(), size.height.toInt())
//            val matSize = Pair(imageAnalysisResolution.width, imageAnalysisResolution.height)
//            latticeLines?.let {
//                drawSegments(
//                    g = this,
//                    segments = it.first.map { line ->
//                        line.toSegment().cvToScreenCoords(screenSize, matSize)
//                    },
//                    color = Color.Red,
//                    strokeWidth = 5f
//                )
//                drawSegments(
//                    g = this,
//                    segments = it.second.map { line ->
//                        line.toSegment().cvToScreenCoords(screenSize, matSize)
//                    },
//                    color = Color.Green,
//                    strokeWidth = 5f
//                )
//
//                val latticePoints = it.first.flatMap { horizontal ->
//                    it.second.mapNotNull { vertical ->
//                        horizontal.intersection(vertical)
//                    }
//                }.map { point -> point.cvToScreenCoords(screenSize, matSize)
//                }.map { point -> Offset(point.x, point.y) }
//
//                drawPoints(
//                    latticePoints,
//                    PointMode.Points,
//                    Color.Blue,
//                    12f
//                )
//            }
//        }
//}

@Composable
fun ImageAnalysisOverlay(
    viewModel: CameraPreviewViewModel
) {
    val imageAnalysisResult by viewModel
        .getImageAnalysisResult().collectAsState(initial = null)

    val imageAnalysisResolution by viewModel.imageAnalysisResolution.collectAsState()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val screenSize = Pair(size.width.toInt(), size.height.toInt())
        val matSize = Pair(imageAnalysisResolution.width, imageAnalysisResolution.height)

        imageAnalysisResult?.let {
            when (it){
                is ImageAnalysisResult.Success -> {
                    it.imageAnalysisState
                    it.imageAnalysisState.cornerPoints?.run { drawPointOverlay(this, screenSize, matSize) }
                }
                is ImageAnalysisResult.Failure -> {
                }
            }
        }

    }
}

fun DrawScope.drawPointOverlay(cornerPoints: List<com.wadiyatalkinabeet.gambit.math.datastructures.Point>, screenSize: Pair<Int, Int>, matSize: Pair<Int, Int>) {
    cornerPoints.map { point -> point.cvToScreenCoords(screenSize, matSize) }
        .map { point -> Offset(point.x, point.y) }
        .let {
            drawPoints(
                it,
                PointMode.Points,
                Color.Blue,
                12f
            )
        }
}

fun drawSegments(g: DrawScope, segments: List<Segment>, color: Color, strokeWidth: Float) {
    for (seg in segments) {
        g.drawLine(
            color,
            start = Offset(seg.p0.x, seg.p0.y),
            end = Offset(seg.p1.x, seg.p1.y),
            strokeWidth=strokeWidth,
        )
    }
}

@Composable
private fun PermissionLayer(onRequestCameraPermission: () -> Unit) {
    val currentOnRequestCameraPermission by rememberUpdatedState(onRequestCameraPermission)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff000000))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.permissions_required),
                color = colorResource(android.R.color.white),
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { currentOnRequestCameraPermission() }) {
                Text(
                    text = stringResource(id = R.string.grant_permissions),
                    fontSize = 13.sp
                )
            }
        }
    }
}