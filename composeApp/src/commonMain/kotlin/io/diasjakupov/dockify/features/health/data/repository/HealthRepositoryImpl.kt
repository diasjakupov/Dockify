package io.diasjakupov.dockify.features.health.data.repository

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.health.data.datasource.HealthLocalDataSource
import io.diasjakupov.dockify.features.health.data.datasource.HealthPlatformDataSource
import io.diasjakupov.dockify.features.health.data.datasource.HealthRemoteDataSource
import io.diasjakupov.dockify.features.health.data.dto.HealthMetricDto
import io.diasjakupov.dockify.features.health.data.mapper.HealthMetricMapper.toDto
import io.diasjakupov.dockify.features.health.data.mapper.HealthMetricMapper.toDomainList
import io.diasjakupov.dockify.features.health.domain.model.HealthData
import io.diasjakupov.dockify.features.health.domain.model.HealthMetric
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType
import io.diasjakupov.dockify.features.health.domain.repository.HealthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of HealthRepository that coordinates between
 * platform-specific, remote, and local data sources.
 */
class HealthRepositoryImpl(
    private val platformDataSource: HealthPlatformDataSource,
    private val remoteDataSource: HealthRemoteDataSource,
    private val localDataSource: HealthLocalDataSource
) : HealthRepository {

    override suspend fun getHealthMetrics(userId: String): Resource<List<HealthMetric>, DataError> {
        return when (val result = remoteDataSource.getHealthMetrics(userId)) {
            is Resource.Success -> {
                // Cache the metrics locally
                localDataSource.cacheMetrics(result.data)
                Resource.Success(result.data.toDomainList())
            }
            is Resource.Error -> {
                // Try to return cached data if remote fails
                when (val cachedResult = localDataSource.getCachedMetrics()) {
                    is Resource.Success -> Resource.Success(cachedResult.data.toDomainList())
                    is Resource.Error -> result
                }
            }
        }
    }

    override suspend fun syncHealthData(healthData: HealthData): EmptyResult<DataError> {
        val requestDto = healthData.toDto()
        return remoteDataSource.createHealthMetrics(requestDto)
    }

    override suspend fun readPlatformHealthData(types: List<HealthMetricType>): Resource<List<HealthMetric>, DataError> {
        return platformDataSource.readHealthData(types)
    }

    override suspend fun isPlatformHealthAvailable(): Boolean {
        return platformDataSource.isAvailable()
    }

    override suspend fun requestHealthPermissions(types: List<HealthMetricType>): EmptyResult<DataError> {
        return platformDataSource.requestPermissions(types)
    }

    override suspend fun hasHealthPermissions(types: List<HealthMetricType>): Boolean {
        return platformDataSource.hasPermissions(types)
    }

    override fun observeCachedMetrics(): Flow<List<HealthMetric>> {
        return localDataSource.observeCachedMetrics().map { dtoList ->
            dtoList.toDomainList()
        }
    }
}
