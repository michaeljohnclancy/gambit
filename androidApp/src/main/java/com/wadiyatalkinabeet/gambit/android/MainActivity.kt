package com.wadiyatalkinabeet.gambit.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.fragment.app.FragmentActivity
import com.github.skgmn.startactivityx.listenPermissionStatus
import com.wadiyatalkinabeet.gambit.android.ui.theme.CameraViewTheme
import android.Manifest
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.github.skgmn.startactivityx.PermissionRequest
import com.github.skgmn.startactivityx.requestPermissions
import com.wadiyatalkinabeet.gambit.CameraPreviewViewModel
import kotlinx.coroutines.launch
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@ExperimentalAnimationApi
class MainActivity : FragmentActivity() {
    private val viewModel: CameraPreviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {

            val systemUiController = rememberSystemUiController()

            val useDarkIcons = false //MaterialTheme.colors.isLight
            SideEffect {
                systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
            }

            CameraViewTheme {
               MainScreen(
                   viewModel = viewModel,
                   permissionStatusFlow = listenPermissionStatus(Manifest.permission.CAMERA),
                   onRequestCameraPermission = {
                       lifecycleScope.launch {
                           requestCameraPermission()
                       }
                   },
               )
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
