package io.diasjakupov.dockify.features.location.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.location.domain.model.Location
import io.diasjakupov.dockify.features.location.domain.repository.LocationRepository

/**
 * Use case for getting the current device location.
 */
class GetCurrentLocationUseCase(
    private val locationRepository: LocationRepository
) {

    /**
     * Gets the current device location with permission checks.
     *
     * @return Resource containing the current Location or an error
     */
    suspend operator fun invoke(): Resource<Location, DataError> {
        if (!locationRepository.hasLocationPermission()) {
            return Resource.Error(DataError.Location.PERMISSION_DENIED)
        }

        if (!locationRepository.isLocationEnabled()) {
            return Resource.Error(DataError.Location.GPS_DISABLED)
        }

        return locationRepository.getCurrentLocation()
    }
}
