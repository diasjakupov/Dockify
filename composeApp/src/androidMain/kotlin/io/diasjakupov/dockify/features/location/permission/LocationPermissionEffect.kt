package io.diasjakupov.dockify.features.location.permission

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState

/**
 * Android implementation of LocationPermissionEffect.
 *
 * Observes the permission handler's trigger and launches the system permission dialog
 * using ActivityResultContracts.
 */
@Composable
actual fun LocationPermissionEffect(permissionHandler: LocationPermissionHandler) {
    val trigger = permissionHandler.permissionRequestTrigger.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        // Permission is granted if either fine or coarse location is granted
        val granted = fineLocationGranted || coarseLocationGranted
        permissionHandler.onPermissionResult(granted)
    }

    // Launch permission request when trigger changes (and is not 0, which is the initial value)
    LaunchedEffect(trigger.value) {
        if (trigger.value > 0) {
            permissionLauncher.launch(LocationPermissionHandler.LOCATION_PERMISSIONS)
        }
    }
}
