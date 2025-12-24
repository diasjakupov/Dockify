package io.diasjakupov.dockify.features.health.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.health.domain.model.HealthMetric
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType

/**
 * Platform-specific data source interface for health data.
 * Implemented differently for Android (Health Connect) and iOS (HealthKit).
 *
 * Use expect/actual pattern for platform-specific implementations.
 */
interface HealthPlatformDataSource {

    /**
     * Checks if the platform health API is available.
     *
     * @return true if available, false otherwise
     */
    suspend fun isAvailable(): Boolean

    /**
     * Checks if permissions are granted for the specified health metric types.
     *
     * @param types The types of health metrics to check permissions for
     * @return true if all permissions are granted, false otherwise
     */
    suspend fun hasPermissions(types: List<HealthMetricType>): Boolean

    /**
     * Requests permissions for the specified health metric types.
     *
     * @param types The types of health metrics to request permissions for
     * @return EmptyResult indicating success or an error
     */
    suspend fun requestPermissions(types: List<HealthMetricType>): EmptyResult<DataError>

    /**
     * Reads health data from the platform health API.
     *
     * @param types The types of health metrics to read
     * @return Resource containing the list of health metrics or an error
     */
    suspend fun readHealthData(types: List<HealthMetricType>): Resource<List<HealthMetric>, DataError>
}
