package com.wadiyatalkinabeet.gambit.android

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.Point
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.skgmn.cameraxx.CameraPreview
import com.github.skgmn.startactivityx.PermissionStatus
import com.wadiyatalkinabeet.gambit.CameraPreviewViewModel
import kotlinx.coroutines.flow.Flow
import kotlin.math.sqrt


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
        LatticeOverlayLayer(viewModel = viewModel)
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

@Composable
fun LatticeOverlayLayer(
    viewModel: CameraPreviewViewModel
) {
    val latticePoints by viewModel
        .getLatticePoints().collectAsState(initial = listOf())

    val imageAnalysisResolution by viewModel.imageAnalysisResolution.collectAsState()

        Canvas(modifier = Modifier.fillMaxSize()
        ) {

                val scale: Float = size.width / imageAnalysisResolution.height

                drawPoints(
                    points = latticePoints.map { Offset(scale * it.x.toFloat(), scale * it.y.toFloat())},
                    pointMode = PointMode.Points,
                    color = Color.Red,
                    strokeWidth = 10f
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

//private fun bindPreview(
//    lifecycleOwner: LifecycleOwner,
//    previewView: PreviewView,
//    cameraProvider: ProcessCameraProvider,
//    analyzer: ImageAnalysis.Analyzer,
//    executor: Executor
//) {
//    val preview = Preview.Builder().build().also {
//        it.setSurfaceProvider(previewView.surfaceProvider)
//    }
//
//    val cameraSelector = CameraSelector.Builder()
//        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//        .build()
//
//    cameraProvider.unbindAll()
//    cameraProvider.bindToLifecycle(
//        lifecycleOwner,
//        cameraSelector,
//        setupImageAnalysis(previewView, executor, analyzer),
//        preview
//    )
//}
//
//private fun setupImageAnalysis(
//    previewView: PreviewView,
//    executor: Executor,
//    analyzer: ImageAnalysis.Analyzer
//): ImageAnalysis {
//    return ImageAnalysis.Builder()
//        .setTargetResolution(Size(previewView.width, previewView.height))
//        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//        .build()
//        .apply {
//            setAnalyzer(executor, analyzer)
//        }
//}
//

//enum class PermissionStatus {
//    GRANTED,
//    DENIED,
//    DO_NOT_ASK_AGAIN;
//
//    val granted: Boolean
//        get() = this === GRANTED
//
//    val denied: Boolean
//        get() = this === DENIED || this === DO_NOT_ASK_AGAIN
//}
