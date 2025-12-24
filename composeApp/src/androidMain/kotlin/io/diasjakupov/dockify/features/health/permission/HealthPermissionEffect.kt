package io.diasjakupov.dockify.features.health.permission

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.health.connect.client.PermissionController

/**
 * Android implementation of HealthPermissionEffect.
 *
 * Observes the permission handler's trigger and launches the Health Connect
 * permission contract using rememberLauncherForActivityResult.
 */
@Composable
actual fun HealthPermissionEffect(permissionHandler: HealthPermissionHandler) {
    val trigger = permissionHandler.permissionRequestTrigger.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        permissionHandler.onPermissionResult(grantedPermissions)
    }

    // Launch permission request when trigger changes (and is not 0, which is the initial value)
    LaunchedEffect(trigger.value) {
        if (trigger.value > 0 && permissionHandler.isHealthConnectAvailable()) {
            permissionLauncher.launch(HealthPermissionHandler.REQUIRED_PERMISSIONS)
        }
    }
}
