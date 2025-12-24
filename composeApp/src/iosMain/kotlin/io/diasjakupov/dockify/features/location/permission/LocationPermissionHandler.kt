package io.diasjakupov.dockify.features.location.permission

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of LocationPermissionHandler.
 *
 * Uses CLLocationManager to request and check location permissions.
 */
@OptIn(ExperimentalForeignApi::class)
actual class LocationPermissionHandler {

    private val locationManager = CLLocationManager()

    actual suspend fun requestLocationPermission(): Boolean {
        val status = locationManager.authorizationStatus

        return when (status) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> true

            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> false

            kCLAuthorizationStatusNotDetermined -> {
                suspendCoroutine { continuation ->
                    val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                            val newStatus = manager.authorizationStatus
                            manager.delegate = null

                            val granted = newStatus == kCLAuthorizationStatusAuthorizedWhenInUse ||
                                    newStatus == kCLAuthorizationStatusAuthorizedAlways

                            continuation.resume(granted)
                        }
                    }
                    locationManager.delegate = delegate
                    locationManager.requestWhenInUseAuthorization()
                }
            }

            else -> false
        }
    }

    actual fun hasLocationPermission(): Boolean {
        val status = locationManager.authorizationStatus
        return status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                status == kCLAuthorizationStatusAuthorizedAlways
    }
}
