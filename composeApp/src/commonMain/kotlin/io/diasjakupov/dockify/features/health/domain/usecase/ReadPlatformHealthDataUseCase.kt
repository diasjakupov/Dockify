package io.diasjakupov.dockify.features.health.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.health.domain.model.HealthMetric
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType
import io.diasjakupov.dockify.features.health.domain.repository.HealthRepository

/**
 * Use case for reading health data from the platform-specific health API.
 */
class ReadPlatformHealthDataUseCase(
    private val healthRepository: HealthRepository
) {

    /**
     * Reads health data from the platform health API (Health Connect / HealthKit).
     *
     * @param types The types of health metrics to read
     * @return Resource containing the list of health metrics or an error
     */
    suspend operator fun invoke(types: List<HealthMetricType>): Resource<List<HealthMetric>, DataError> {
        if (types.isEmpty()) {
            return Resource.Success(emptyList())
        }

        if (!healthRepository.isPlatformHealthAvailable()) {
            return Resource.Error(DataError.Health.HEALTH_CONNECT_NOT_AVAILABLE)
        }

        if (!healthRepository.hasHealthPermissions(types)) {
            return Resource.Error(DataError.Health.PERMISSION_DENIED)
        }

        return healthRepository.readPlatformHealthData(types)
    }
}
