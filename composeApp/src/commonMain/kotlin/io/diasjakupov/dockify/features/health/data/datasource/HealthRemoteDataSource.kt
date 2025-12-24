package io.diasjakupov.dockify.features.health.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.health.data.dto.HealthMetricDto
import io.diasjakupov.dockify.features.health.data.dto.HealthMetricsRequestDto

/**
 * Remote data source interface for health metrics API calls.
 */
interface HealthRemoteDataSource {

    /**
     * Fetches health metrics from the backend for a specific user.
     *
     * @param userId The user's ID
     * @return Resource containing the list of health metric DTOs or an error
     */
    suspend fun getHealthMetrics(userId: String): Resource<List<HealthMetricDto>, DataError>

    /**
     * Creates/uploads health metrics to the backend.
     *
     * @param request The health metrics request DTO
     * @return EmptyResult indicating success or an error
     */
    suspend fun createHealthMetrics(request: HealthMetricsRequestDto): EmptyResult<DataError>
}
