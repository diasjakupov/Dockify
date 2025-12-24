package io.diasjakupov.dockify.features.location.permission

/**
 * Platform-specific handler for location permission requests.
 *
 * On Android: Uses Activity result contracts to request permissions.
 * On iOS: Uses CLLocationManager.requestWhenInUseAuthorization().
 */
expect class LocationPermissionHandler {
    /**
     * Requests location permission from the system.
     * Suspends until the user responds to the permission dialog.
     *
     * @return true if permission was granted, false otherwise
     */
    suspend fun requestLocationPermission(): Boolean

    /**
     * Checks if location permission is currently granted.
     *
     * @return true if permission is granted, false otherwise
     */
    fun hasLocationPermission(): Boolean
}
