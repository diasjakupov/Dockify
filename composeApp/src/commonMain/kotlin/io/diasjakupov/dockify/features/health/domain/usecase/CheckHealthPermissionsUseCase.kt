package io.diasjakupov.dockify.features.health.domain.usecase

import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType
import io.diasjakupov.dockify.features.health.domain.repository.HealthRepository

/**
 * Use case for checking platform health availability and permissions.
 * Provides a clean interface for the ViewModel to check prerequisites
 * before reading health data.
 */
class CheckHealthPermissionsUseCase(
    private val healthRepository: HealthRepository
) {
    /**
     * Checks if the platform health API is available.
     * (Health Connect on Android, HealthKit on iOS)
     */
    suspend fun isPlatformAvailable(): Boolean =
        healthRepository.isPlatformHealthAvailable()

    /**
     * Checks if permissions are granted for the specified health metric types.
     */
    suspend fun hasPermissions(types: List<HealthMetricType>): Boolean =
        healthRepository.hasHealthPermissions(types)
}
