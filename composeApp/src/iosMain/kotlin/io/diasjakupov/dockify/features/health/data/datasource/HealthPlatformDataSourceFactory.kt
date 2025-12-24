package io.diasjakupov.dockify.features.health.data.datasource

import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType

/**
 * iOS actual implementation of HealthPlatformDataSourceFactory.
 * Creates IOSHealthPlatformDataSource using HealthKit.
 */
actual class HealthPlatformDataSourceFactory {
    /**
     * Creates an IOSHealthPlatformDataSource instance.
     */
    actual fun create(): HealthPlatformDataSource {
        return IOSHealthPlatformDataSource()
    }
}

/**
 * Returns an empty set for iOS as HealthKit doesn't use string-based permissions like Android.
 * On iOS, permissions are handled through HKObjectType-based authorization.
 */
actual fun getRequiredHealthPermissions(types: List<HealthMetricType>): Set<String> {
    // iOS HealthKit doesn't use string-based permissions
    // Authorization is handled through HKHealthStore.requestAuthorizationToShareTypes
    return emptySet()
}
