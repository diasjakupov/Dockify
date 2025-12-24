package io.diasjakupov.dockify.features.location.domain.repository

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.location.domain.model.Hospital
import io.diasjakupov.dockify.features.location.domain.model.Location
import io.diasjakupov.dockify.features.location.domain.model.NearbyUser
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for location operations.
 * Domain layer defines this contract; data layer provides implementation.
 */
interface LocationRepository {

    /**
     * Gets the current device location.
     *
     * @return Resource containing the current Location or an error
     */
    suspend fun getCurrentLocation(): Resource<Location, DataError>

    /**
     * Finds users nearest to the specified location.
     *
     * @param location The center point for the search
     * @param radiusMeters The search radius in meters
     * @param currentUserId The current user's ID (to exclude from results)
     * @return Resource containing the list of nearby users or an error
     */
    suspend fun getNearestUsers(
        location: Location,
        radiusMeters: Double,
        currentUserId: String
    ): Resource<List<NearbyUser>, DataError>

    /**
     * Finds hospitals nearest to the specified location.
     *
     * @param location The center point for the search
     * @param radiusMeters The search radius in meters
     * @return Resource containing the list of nearby hospitals or an error
     */
    suspend fun getNearestHospitals(
        location: Location,
        radiusMeters: Double
    ): Resource<List<Hospital>, DataError>

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
    suspend fun hasLocationPermission(): Boolean

    /**
     * Requests location permission.
     *
     * @return EmptyResult indicating success or an error
     */
    suspend fun requestLocationPermission(): EmptyResult<DataError>

    /**
     * Checks if location services (GPS) are enabled.
     *
     * @return true if enabled, false otherwise
     */
    suspend fun isLocationEnabled(): Boolean
}
