package io.diasjakupov.dockify.features.location.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.location.domain.model.Hospital
import io.diasjakupov.dockify.features.location.domain.model.Location
import io.diasjakupov.dockify.features.location.domain.repository.LocationRepository

/**
 * Use case for finding hospitals nearest to a location.
 */
class GetNearestHospitalsUseCase(
    private val locationRepository: LocationRepository
) {

    /**
     * Finds hospitals nearest to the specified location.
     *
     * @param location The center point for the search
     * @param radiusMeters The search radius in meters
     * @return Resource containing the list of nearby hospitals or an error
     */
    suspend operator fun invoke(
        location: Location,
        radiusMeters: Double
    ): Resource<List<Hospital>, DataError> {
        if (!location.isValid()) {
            return Resource.Error(DataError.Location.LOCATION_UNAVAILABLE)
        }

        if (radiusMeters <= 0) {
            return Resource.Error(DataError.Location.LOCATION_UNAVAILABLE)
        }

        return locationRepository.getNearestHospitals(location, radiusMeters)
    }
}
