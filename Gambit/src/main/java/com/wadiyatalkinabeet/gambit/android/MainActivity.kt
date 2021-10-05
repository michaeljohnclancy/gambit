package com.wadiyatalkinabeet.gambit.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.fragment.app.FragmentActivity
import com.github.skgmn.startactivityx.listenPermissionStatus
import com.wadiyatalkinabeet.gambit.CameraPreviewViewModel
import com.wadiyatalkinabeet.gambit.android.ui.theme.CameraViewTheme
import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.github.skgmn.startactivityx.PermissionRequest
import com.github.skgmn.startactivityx.requestPermissions
import com.google.accompanist.insets.ProvideWindowInsets
import com.wadiyatalkinabeet.gambit.android.chess.GameLogComposable
import com.wadiyatalkinabeet.gambit.chess.GameLogViewModel
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
class MainActivity : FragmentActivity() {
    private val viewModel: CameraPreviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            CameraViewTheme {
                ProvideWindowInsets {
                    Box(
                        Modifier.background(MaterialTheme.colors.background)
                    ) {
                        GameLogComposable(GameLogViewModel())
//                       MainScreen(
//                           viewModel = viewModel,
//                           permissionStatusFlow = listenPermissionStatus(Manifest.permission.CAMERA),
//                           onRequestCameraPermission = {
//                               lifecycleScope.launch {
//                                   requestCameraPermission()
//                               }
//                           },
//                       )
                    }
                }
            }
        }

        lifecycleScope.launch {
            requestPermissions(Manifest.permission.CAMERA)
            viewModel.permissionsInitiallyRequestedState.value = true
        }
    }

    private suspend fun requestCameraPermission() {
        val permissionRequest = PermissionRequest(listOf(Manifest.permission.CAMERA), true)
            requestPermissions(permissionRequest)
    }
}
