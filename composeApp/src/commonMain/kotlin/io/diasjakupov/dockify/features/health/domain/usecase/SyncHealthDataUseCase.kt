package io.diasjakupov.dockify.features.health.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.health.domain.model.HealthData
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType
import io.diasjakupov.dockify.features.health.domain.repository.HealthRepository
import io.diasjakupov.dockify.features.location.domain.model.Location

/**
 * Use case for syncing health data from the device to the backend.
 * Orchestrates reading from the platform health API and uploading to the server.
 */
class SyncHealthDataUseCase(
    private val healthRepository: HealthRepository
) {

    /**
     * Syncs health data for a user.
     * Reads data from the platform health API and uploads it to the backend.
     *
     * @param userId The user's ID
     * @param types The types of health metrics to sync
     * @param location Optional current location to include with the sync
     * @return EmptyResult indicating success or an error
     */
    suspend operator fun invoke(
        userId: String,
        types: List<HealthMetricType>,
        location: Location? = null
    ): EmptyResult<DataError> {
        if (userId.isBlank()) {
            return Resource.Error(DataError.Auth.UNAUTHORIZED)
        }

        if (types.isEmpty()) {
            return Resource.Error(DataError.Health.DATA_NOT_FOUND)
        }

        // Check if platform health is available
        if (!healthRepository.isPlatformHealthAvailable()) {
            return Resource.Error(DataError.Health.HEALTH_CONNECT_NOT_AVAILABLE)
        }

        // Check permissions
        if (!healthRepository.hasHealthPermissions(types)) {
            return Resource.Error(DataError.Health.PERMISSION_DENIED)
        }

        // Read data from platform
        val platformDataResult = healthRepository.readPlatformHealthData(types)
        val metrics = when (platformDataResult) {
            is Resource.Success -> platformDataResult.data
            is Resource.Error -> return platformDataResult
        }

        if (metrics.isEmpty()) {
            return Resource.Error(DataError.Health.DATA_NOT_FOUND)
        }

        // Create health data and sync to backend
        val healthData = HealthData(
            userId = userId,
            metrics = metrics,
            location = location
        )

        return healthRepository.syncHealthData(healthData)
    }
}
