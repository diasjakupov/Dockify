package io.diasjakupov.dockify.features.location.permission

import androidx.compose.runtime.Composable

/**
 * Platform-specific composable that handles location permission requests.
 *
 * On Android: Observes the permission handler's trigger and launches the system permission dialog.
 * On iOS: No-op since iOS handles permission requests internally via CLLocationManager.
 *
 * @param permissionHandler The platform-specific permission handler
 */
@Composable
expect fun LocationPermissionEffect(permissionHandler: LocationPermissionHandler)
