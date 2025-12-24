package io.diasjakupov.dockify.features.location.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.location.domain.model.Location
import io.diasjakupov.dockify.features.location.domain.model.NearbyUser
import io.diasjakupov.dockify.features.location.domain.repository.LocationRepository

/**
 * Use case for finding users nearest to a location.
 */
class GetNearestUsersUseCase(
    private val locationRepository: LocationRepository
) {

    /**
     * Finds users nearest to the specified location.
     *
     * @param location The center point for the search
     * @param radiusMeters The search radius in meters
     * @param currentUserId The current user's ID (to exclude from results)
     * @return Resource containing the list of nearby users or an error
     */
    suspend operator fun invoke(
        location: Location,
        radiusMeters: Double,
        currentUserId: String
    ): Resource<List<NearbyUser>, DataError> {
        if (!location.isValid()) {
            return Resource.Error(DataError.Location.LOCATION_UNAVAILABLE)
        }

        if (radiusMeters <= 0) {
            return Resource.Error(DataError.Location.LOCATION_UNAVAILABLE)
        }

        if (currentUserId.isBlank()) {
            return Resource.Error(DataError.Auth.UNAUTHORIZED)
        }

        return locationRepository.getNearestUsers(location, radiusMeters, currentUserId)
    }
}
