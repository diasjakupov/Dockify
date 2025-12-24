package io.diasjakupov.dockify.features.location.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.location.domain.repository.LocationRepository

/**
 * Use case for requesting location permission.
 */
class RequestLocationPermissionUseCase(
    private val locationRepository: LocationRepository
) {

    /**
     * Requests location permission if not already granted.
     *
     * @return EmptyResult indicating success or an error
     */
    suspend operator fun invoke(): EmptyResult<DataError> {
        if (locationRepository.hasLocationPermission()) {
            return Resource.Success(Unit)
        }

        return locationRepository.requestLocationPermission()
    }
}
