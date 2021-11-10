package com.wadiyatalkinabeet.gambit.android

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.skgmn.cameraxx.CameraPreview
import com.github.skgmn.startactivityx.PermissionStatus
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.systemBarsPadding
import com.wadiyatalkinabeet.gambit.CameraPreviewViewModel
import com.wadiyatalkinabeet.gambit.Resource
import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Point
import com.wadiyatalkinabeet.gambit.domain.math.datastructures.Segment
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
        ViewFinder(viewModel)
    }
    if (permissionInitiallyRequested && permissionStatus?.denied == true) {
        PermissionLayer(onRequestCameraPermission)
    }
}

@Composable
private fun ViewFinder(
    viewModel: CameraPreviewViewModel
) {
    val context = LocalContext.current

    // Camera view with overlay
    CameraLayer(viewModel = viewModel)
    ImageAnalysisOverlay(viewModel = viewModel)

    // Tooltip
    Tooltip("Point your phone at a chessboard")

    // Status bar shadow
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(with(LocalDensity.current) {
                (3 * LocalWindowInsets.current.systemBars.top).toDp()
            })
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.75f),
                        Color.Transparent
                    ),
                )
            )
    )

    // Back button
    IconButton(
        onClick = {
            Toast.makeText(context, "Can't go back", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier
            .systemBarsPadding()
            .padding(12.dp)
    ) {
        Icon(
            Icons.Filled.ArrowBack,
            "Back",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}

fun lerp(start: Float, end: Float, fraction: Float) = start * (1f - fraction) + end * fraction

@Composable
private fun Tooltip(
    text: String
) {
    Box(
        Modifier.fillMaxSize()
            .navigationBarsWithImePadding()
            .padding(bottom=24.dp)
    ) {
        Surface(
            Modifier.align(Alignment.BottomCenter),
            color = Color.Black.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text,
                Modifier.padding(horizontal=24.dp, vertical=12.dp),
                color = Color.White
            )
        }
    }
}

@Composable
private fun Reticle(
    points: List<Point>, matSize: Size
) {
    val pulseAnim = rememberInfiniteTransition()
    val glowScale by pulseAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, 0, LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val auraScale by pulseAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, 1000, FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(Modifier.fillMaxSize()) {
        fun quadPath(points: List<Point>, scale: Float) = Path().apply {
            val c = points.fold(Point(0f, 0f)) { a, b -> a + b } / 4f
            val ps = points.map {
                it + (it - c) * scale
            }
            moveTo(ps[0].x, ps[0].y)
            lineTo(ps[1].x, ps[1].y)
            lineTo(ps[2].x, ps[2].y)
            lineTo(ps[3].x, ps[3].y)
            close()
        }

        val r = 20f
        val mainStyle = Stroke(
            width = 6f,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.cornerPathEffect(r)
        )
        val auraStyle = Stroke(
            width = lerp(6f, 0f, auraScale),
            join = StrokeJoin.Round,
            pathEffect = PathEffect.cornerPathEffect(
                lerp(r, r * 1.5f, auraScale)
            )
        )

        val mainSquare = quadPath(points, 0f)
        val auraSquare = quadPath(points, lerp(0f, 0.2f, auraScale))
        cvToScreenCoords(matSize) {
            drawPath(mainSquare, Color.White, 0.5f, mainStyle)
            drawPath(auraSquare, Color.White, lerp(0f, 0.5f, glowScale), auraStyle)
        }
    }
}

@Composable
private fun CameraLayer(
    viewModel: CameraPreviewViewModel
) {
    val preview by remember { mutableStateOf(viewModel.preview) }
    val imageAnalysis by viewModel.imageAnalysisUseCaseState.collectAsState()

    CameraPreview(
        modifier = Modifier.fillMaxSize(),
        preview = preview,
        scaleType = PreviewView.ScaleType.FILL_CENTER,
        imageAnalysis = imageAnalysis
    )
}

@Composable
fun ImageAnalysisOverlay(
    viewModel: CameraPreviewViewModel
) {
    val imageAnalysisResult by viewModel
        .imageAnalysisResult.collectAsState(initial = null)

    val imageAnalysisResolution by viewModel.imageAnalysisResolution.collectAsState()

    val lineColor = Color.White.copy(alpha=0.7f)

    val matSize = Size(
        imageAnalysisResolution.width.toFloat(),
        imageAnalysisResolution.height.toFloat()
    )

    // Location of reticule corners when no real corners are available
    val defaultPoints = listOf(
            Point(-1f, -1f), Point(-1f, 1f),
            Point(1f, 1f), Point(1f, -1f)
        ).map { sign ->
            Point(matSize.width / 2, matSize.height / 2) + sign * (matSize.height / 5f)
        }

    var points by remember { mutableStateOf (defaultPoints) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        imageAnalysisResult?.let { result ->
            when (result){
                is Resource.Loading -> {
                    cvToScreenCoords(
                        matSize
                    ) {
                        result.data?.horizontalLines
                            ?.map { line -> line.toSegment() }
                            ?.let { lines -> drawSegments(lines, lineColor, 2f) }
                        result.data?.verticalLines
                            ?.map { line -> line.toSegment() }
                            ?.let { lines -> drawSegments(lines, lineColor, 2f) }
//                        it.data?.cornerPoints
//                            ?.run { drawPointOverlay(this) }
                        result.data?.cornerPoints?.let{ points = it }
                    }

                }
                is Resource.Success -> {
                    cvToScreenCoords(
                        matSize
                    ) {
                        result.data?.horizontalLines
                            ?.map { line -> line.toSegment() }
                            ?.let { lines -> drawSegments(lines, lineColor, 2f) }
                        result.data?.verticalLines
                            ?.map { line -> line.toSegment() }
                            ?.let { lines -> drawSegments(lines, lineColor, 2f) }
//                        it.data?.cornerPoints
//                            ?.run { drawPointOverlay(this) }
                        result.data?.cornerPoints?.let{ points = it }
                    }
                }
                is Resource.Error -> {
                    points = defaultPoints
                }
            }
        }
    }

    Reticle(points, matSize)
}

fun DrawScope.cvToScreenCoords(
    matSize: Size,
    block: DrawScope.() -> Unit
) {
    val scale = size.height / matSize.width
    drawContext.transform.scale(scale, scale, Offset(0f, 0f))
    drawContext.transform.rotate(90f, Offset(0f, 0f))
    //TODO The approximate decimal number below needs an exact equation
    drawContext.transform.translate(0f, -matSize.height * 0.815f)
    block()
// Uncomment for debug rect
// Top: Black
// Left: Yellow
// Right: Cyan
// Bottom: White
//    drawSegments(
//        listOf(Segment(0f, 0f, matSize.width, 0f)),
//        Color.Cyan, 5f
//    )
//    drawSegments(
//        listOf(Segment(0f, matSize.height, matSize.width, matSize.height)),
//        Color.Yellow, 5f
//    )
//
//    drawSegments(
//        listOf(Segment(0f, 0f, 0f, matSize.height)),
//        Color.Black, 5f
//    )
//    drawSegments(
//        listOf(Segment(matSize.width, 0f, matSize.width, matSize.height)),
//        Color.White, 5f
//    )
    drawContext.transform.translate(0f, matSize.height * 0.815f)
    drawContext.transform.rotate(-90f, Offset(0f, 0f))
    drawContext.transform.scale(1/scale, 1/scale, Offset(0f, 0f))
}

fun DrawScope.drawPointOverlay(cornerPoints: List<Point>) =
    cornerPoints
        .map { point -> Offset(point.x, point.y) }
        .let {
            drawPoints(
                it,
                PointMode.Points,
                Color(1f, 0f, 0f, 1f),
                12f
            )
        }

fun DrawScope.drawSegments(segments: List<Segment>, color: Color, strokeWidth: Float) {
    for (seg in segments) {
        drawLine(
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