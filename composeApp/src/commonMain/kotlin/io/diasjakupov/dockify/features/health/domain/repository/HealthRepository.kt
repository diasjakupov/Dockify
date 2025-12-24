package io.diasjakupov.dockify.features.health.domain.repository

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.health.domain.model.HealthData
import io.diasjakupov.dockify.features.health.domain.model.HealthMetric
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for health data operations.
 * Domain layer defines this contract; data layer provides implementation.
 */
interface HealthRepository {

    /**
     * Fetches health metrics from the backend for a specific user.
     *
     * @param userId The user's ID
     * @return Resource containing the list of health metrics or an error
     */
    suspend fun getHealthMetrics(userId: String): Resource<List<HealthMetric>, DataError>

    /**
     * Syncs health data to the backend.
     *
     * @param healthData The health data to sync
     * @return EmptyResult indicating success or an error
     */
    suspend fun syncHealthData(healthData: HealthData): EmptyResult<DataError>

    /**
     * Reads health data from the platform-specific health API
     * (Health Connect on Android, HealthKit on iOS).
     *
     * @param types The types of health metrics to read
     * @return Resource containing the list of health metrics or an error
     */
    suspend fun readPlatformHealthData(types: List<HealthMetricType>): Resource<List<HealthMetric>, DataError>

    /**
     * Checks if the platform health API is available.
     *
     * @return true if available, false otherwise
     */
    suspend fun isPlatformHealthAvailable(): Boolean

    /**
     * Requests permissions for the specified health metric types.
     *
     * @param types The types of health metrics to request permissions for
     * @return EmptyResult indicating success or an error
     */
    suspend fun requestHealthPermissions(types: List<HealthMetricType>): EmptyResult<DataError>

    /**
     * Checks if permissions are granted for the specified health metric types.
     *
     * @param types The types of health metrics to check permissions for
     * @return true if all permissions are granted, false otherwise
     */
    suspend fun hasHealthPermissions(types: List<HealthMetricType>): Boolean

    /**
     * Observes cached health metrics from local storage.
     *
     * @return Flow emitting the list of cached health metrics
     */
    fun observeCachedMetrics(): Flow<List<HealthMetric>>
}
