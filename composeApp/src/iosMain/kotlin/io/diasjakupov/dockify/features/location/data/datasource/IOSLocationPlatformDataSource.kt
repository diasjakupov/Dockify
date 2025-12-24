package io.diasjakupov.dockify.features.location.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.location.domain.model.Location
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of LocationPlatformDataSource using CoreLocation.
 */
@OptIn(ExperimentalForeignApi::class)
class IOSLocationPlatformDataSource : LocationPlatformDataSource {

    private val locationManager = CLLocationManager()

    override suspend fun getCurrentLocation(): Resource<Location, DataError> {
        if (!hasPermission()) {
            return Resource.Error(DataError.Location.PERMISSION_DENIED)
        }

        if (!isLocationEnabled()) {
            return Resource.Error(DataError.Location.GPS_DISABLED)
        }

        return suspendCoroutine { continuation ->
            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    val clLocation = didUpdateLocations.lastOrNull() as? CLLocation
                    if (clLocation != null) {
                        val location = clLocation.coordinate.useContents {
                            Location(
                                latitude = latitude,
                                longitude = longitude
                            )
                        }
                        manager.stopUpdatingLocation()
                        manager.delegate = null
                        continuation.resume(Resource.Success(location))
                    }
                }

                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                    manager.stopUpdatingLocation()
                    manager.delegate = null
                    continuation.resume(Resource.Error(DataError.Location.LOCATION_UNAVAILABLE))
                }
            }

            locationManager.delegate = delegate
            locationManager.desiredAccuracy = kCLLocationAccuracyBest
            locationManager.startUpdatingLocation()
        }
    }

    override fun observeLocation(): Flow<Resource<Location, DataError>> = callbackFlow {
        if (!hasPermission()) {
            trySend(Resource.Error(DataError.Location.PERMISSION_DENIED))
            close()
            return@callbackFlow
        }

        if (!isLocationEnabled()) {
            trySend(Resource.Error(DataError.Location.GPS_DISABLED))
            close()
            return@callbackFlow
        }

        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val clLocation = didUpdateLocations.lastOrNull() as? CLLocation
                if (clLocation != null) {
                    val location = clLocation.coordinate.useContents {
                        Location(
                            latitude = latitude,
                            longitude = longitude
                        )
                    }
                    trySend(Resource.Success(location))
                }
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                trySend(Resource.Error(DataError.Location.LOCATION_UNAVAILABLE))
            }
        }

        locationManager.delegate = delegate
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.startUpdatingLocation()

        awaitClose {
            locationManager.stopUpdatingLocation()
            locationManager.delegate = null
        }
    }

    override suspend fun hasPermission(): Boolean {
        val status = locationManager.authorizationStatus
        return status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                status == kCLAuthorizationStatusAuthorizedAlways
    }

    override suspend fun requestPermission(): EmptyResult<DataError> {
        val status = locationManager.authorizationStatus

        return when (status) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> Resource.Success(Unit)
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> Resource.Error(DataError.Location.PERMISSION_DENIED)
            kCLAuthorizationStatusNotDetermined -> {
                suspendCoroutine { continuation ->
                    val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                            val newStatus = manager.authorizationStatus
                            manager.delegate = null
                            when (newStatus) {
                                kCLAuthorizationStatusAuthorizedWhenInUse,
                                kCLAuthorizationStatusAuthorizedAlways -> {
                                    continuation.resume(Resource.Success(Unit))
                                }
                                else -> {
                                    continuation.resume(Resource.Error(DataError.Location.PERMISSION_DENIED))
                                }
                            }
                        }
                    }
                    locationManager.delegate = delegate
                    locationManager.requestWhenInUseAuthorization()
                }
            }
            else -> Resource.Error(DataError.Location.PERMISSION_DENIED)
        }
    }

    override suspend fun isLocationEnabled(): Boolean {
        return CLLocationManager.locationServicesEnabled()
    }
}
