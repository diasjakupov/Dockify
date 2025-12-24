package io.diasjakupov.dockify.features.health.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.health.data.dto.HealthMetricDto
import kotlinx.coroutines.flow.Flow

/**
 * Local data source interface for caching health metrics.
 */
interface HealthLocalDataSource {

    /**
     * Caches health metrics locally.
     *
     * @param metrics The list of health metric DTOs to cache
     * @return EmptyResult indicating success or a local storage error
     */
    suspend fun cacheMetrics(metrics: List<HealthMetricDto>): EmptyResult<DataError>

    /**
     * Retrieves cached health metrics.
     *
     * @return Resource containing the list of cached metrics or an error
     */
    suspend fun getCachedMetrics(): Resource<List<HealthMetricDto>, DataError>

    /**
     * Observes cached health metrics as a Flow.
     *
     * @return Flow emitting the list of cached health metrics
     */
    fun observeCachedMetrics(): Flow<List<HealthMetricDto>>

    /**
     * Clears the health metrics cache.
     *
     * @return EmptyResult indicating success or a local storage error
     */
    suspend fun clearCache(): EmptyResult<DataError>
}
