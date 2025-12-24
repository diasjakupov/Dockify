package io.diasjakupov.dockify.features.health.permission

import androidx.compose.runtime.Composable

/**
 * Platform-specific composable that handles health permission requests.
 *
 * On Android: Observes the permission handler's trigger and launches the Health Connect permission contract.
 * On iOS: No-op since iOS handles permission requests internally via HKHealthStore.
 *
 * @param permissionHandler The platform-specific health permission handler
 */
@Composable
expect fun HealthPermissionEffect(permissionHandler: HealthPermissionHandler)
