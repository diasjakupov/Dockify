package io.diasjakupov.dockify.features.health.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.health.domain.model.HealthMetric
import io.diasjakupov.dockify.features.health.domain.repository.HealthRepository

/**
 * Use case for fetching health metrics from the backend.
 */
class GetHealthMetricsUseCase(
    private val healthRepository: HealthRepository
) {

    /**
     * Fetches health metrics for a specific user.
     *
     * @param userId The user's ID
     * @return Resource containing the list of health metrics or an error
     */
    suspend operator fun invoke(userId: String): Resource<List<HealthMetric>, DataError> {
        if (userId.isBlank()) {
            return Resource.Error(DataError.Auth.UNAUTHORIZED)
        }
        return healthRepository.getHealthMetrics(userId)
    }
}
