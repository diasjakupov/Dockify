package io.diasjakupov.dockify.features.health.data.datasource

import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType

/**
 * Factory for creating platform-specific HealthPlatformDataSource instances.
 * Uses expect/actual pattern for KMP.
 */
expect class HealthPlatformDataSourceFactory {
    /**
     * Creates a platform-specific HealthPlatformDataSource implementation.
     */
    fun create(): HealthPlatformDataSource
}

/**
 * Returns the set of permission strings required for the given health metric types.
 * Platform-specific implementation maps HealthMetricType to platform permission strings.
 */
expect fun getRequiredHealthPermissions(types: List<HealthMetricType>): Set<String>
