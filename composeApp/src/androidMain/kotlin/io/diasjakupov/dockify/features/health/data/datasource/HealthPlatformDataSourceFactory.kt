package io.diasjakupov.dockify.features.health.data.datasource

import android.content.Context
import io.diasjakupov.dockify.features.health.data.mapper.AndroidHealthMetricMapper.toHealthConnectPermissions
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType

/**
 * Android actual implementation of HealthPlatformDataSourceFactory.
 * Creates AndroidHealthPlatformDataSource using Health Connect.
 */
actual class HealthPlatformDataSourceFactory(
    private val context: Context
) {
    /**
     * Creates an AndroidHealthPlatformDataSource instance.
     */
    actual fun create(): HealthPlatformDataSource {
        return AndroidHealthPlatformDataSource(context)
    }
}

/**
 * Returns the set of Health Connect permission strings required for the given health metric types.
 */
actual fun getRequiredHealthPermissions(types: List<HealthMetricType>): Set<String> {
    return types.toHealthConnectPermissions()
}
