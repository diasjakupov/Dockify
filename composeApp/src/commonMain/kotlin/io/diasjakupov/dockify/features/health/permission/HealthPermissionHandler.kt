package io.diasjakupov.dockify.features.health.permission

/**
 * Platform-specific handler for health permission requests.
 *
 * On Android: Uses Health Connect's permission contract to request permissions.
 * On iOS: Uses HKHealthStore.requestAuthorizationToShareTypes().
 */
expect class HealthPermissionHandler {
    /**
     * Requests health permissions from the system.
     * Suspends until the user responds to the permission dialog.
     *
     * @return true if permissions were granted, false otherwise
     */
    suspend fun requestHealthPermissions(): Boolean

    /**
     * Checks if health permissions are currently granted.
     *
     * @return true if permissions are granted, false otherwise
     */
    fun hasHealthPermissions(): Boolean
}
