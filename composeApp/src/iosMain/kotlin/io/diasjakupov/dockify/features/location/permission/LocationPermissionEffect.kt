package io.diasjakupov.dockify.features.location.permission

import androidx.compose.runtime.Composable

/**
 * iOS implementation of LocationPermissionEffect.
 *
 * No-op since iOS handles permission requests internally via CLLocationManager.requestWhenInUseAuthorization().
 */
@Composable
actual fun LocationPermissionEffect(permissionHandler: LocationPermissionHandler) {
    // No-op on iOS - permission requests are handled internally by CLLocationManager
}
