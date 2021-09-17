package com.wadiyatalkinabeet.gambit.android

import android.graphics.Point
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.skgmn.cameraxx.CameraPreview
import com.github.skgmn.startactivityx.PermissionStatus
import com.wadiyatalkinabeet.gambit.CameraPreviewViewModel
import kotlinx.coroutines.flow.Flow


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
        CameraLayer(cameraPreviewViewModel = viewModel)
    }
    if (permissionInitiallyRequested && permissionStatus?.denied == true) {
        PermissionLayer(onRequestCameraPermission)
    }
//    if (latticePoints.isNotEmpty()){
//        LatticeOverlayLayer(latticePoints)
//    }
}

@Composable
private fun CameraLayer(
    cameraPreviewViewModel: CameraPreviewViewModel
){
    val preview by remember { mutableStateOf(cameraPreviewViewModel.preview) }
    val imageAnalysis by cameraPreviewViewModel.imageAnalysisState.collectAsState()

    Box(
        modifier = Modifier
            .background(Color(0xff000000))
            .fillMaxSize()
    ) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            preview = preview,
//            imageAnalysis = imageAnalysis
        )
    }
}

@ExperimentalAnimationApi
@Composable
private fun LatticeOverlayLayer(viewModel: CameraPreviewViewModel) {
    val latticePoints by viewModel.latticePoints.collectAsState()

    AnimatedVisibility(visible = latticePoints.isNotEmpty()) {
        val paint = Paint().asFrameworkPaint()
        Canvas(modifier = Modifier.fillMaxSize()) {
            paint.apply {
                isAntiAlias = true
                textSize = 48f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = android.graphics.Color.WHITE
            }
            drawIntoCanvas { canvas -> latticePoints.forEach { canvas.nativeCanvas.drawPoint(it.x.toFloat(), it.y.toFloat(), paint) }  }
        }
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
