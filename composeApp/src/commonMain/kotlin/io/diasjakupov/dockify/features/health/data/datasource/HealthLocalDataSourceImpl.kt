package io.diasjakupov.dockify.features.health.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.health.data.dto.HealthMetricDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory implementation of HealthLocalDataSource.
 * Caches health metrics in memory with Flow-based observation.
 */
class HealthLocalDataSourceImpl : HealthLocalDataSource {

    private val cachedMetrics = MutableStateFlow<List<HealthMetricDto>>(emptyList())

    override suspend fun cacheMetrics(metrics: List<HealthMetricDto>): EmptyResult<DataError> {
        return try {
            cachedMetrics.value = metrics
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(DataError.Local.WRITE_ERROR)
        }
    }

    override suspend fun getCachedMetrics(): Resource<List<HealthMetricDto>, DataError> {
        return try {
            val metrics = cachedMetrics.value
            if (metrics.isEmpty()) {
                Resource.Error(DataError.Local.NOT_FOUND)
            } else {
                Resource.Success(metrics)
            }
        } catch (e: Exception) {
            Resource.Error(DataError.Local.READ_ERROR)
        }
    }

    override fun observeCachedMetrics(): Flow<List<HealthMetricDto>> {
        return cachedMetrics.asStateFlow()
    }

    override suspend fun clearCache(): EmptyResult<DataError> {
        return try {
            cachedMetrics.value = emptyList()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(DataError.Local.WRITE_ERROR)
        }
    }
}
