package io.diasjakupov.dockify.features.health.permission

import androidx.compose.runtime.Composable

/**
 * iOS implementation of HealthPermissionEffect.
 *
 * No-op since iOS handles permission requests internally via HKHealthStore.requestAuthorizationToShareTypes().
 */
@Composable
actual fun HealthPermissionEffect(permissionHandler: HealthPermissionHandler) {
    // No-op on iOS - permission requests are handled internally by HKHealthStore
}
