package io.diasjakupov.dockify.features.location.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.location.domain.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Platform-specific data source interface for location services.
 * Implemented differently for Android (FusedLocationProvider) and iOS (CoreLocation).
 *
 * Use expect/actual pattern for platform-specific implementations.
 */
interface LocationPlatformDataSource {

    /**
     * Gets the current device location.
     *
     * @return Resource containing the current Location or an error
     */
    suspend fun getCurrentLocation(): Resource<Location, DataError>

    /**
     * Observes location updates as a Flow.
     *
     * @return Flow emitting location updates or errors
     */
    fun observeLocation(): Flow<Resource<Location, DataError>>

    /**
     * Checks if location permission is granted.
     *
     * @return true if permission is granted, false otherwise
     */
    suspend fun hasPermission(): Boolean

    /**
     * Requests location permission.
     *
     * @return EmptyResult indicating success or an error
     */
    suspend fun requestPermission(): EmptyResult<DataError>

    /**
     * Checks if location services (GPS) are enabled.
     *
     * @return true if enabled, false otherwise
     */
    suspend fun isLocationEnabled(): Boolean
}
