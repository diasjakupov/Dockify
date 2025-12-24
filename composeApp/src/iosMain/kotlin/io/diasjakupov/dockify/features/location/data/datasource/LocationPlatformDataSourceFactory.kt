package io.diasjakupov.dockify.features.location.data.datasource

/**
 * iOS actual implementation of LocationPlatformDataSourceFactory.
 * Creates IOSLocationPlatformDataSource using CoreLocation.
 */
actual class LocationPlatformDataSourceFactory {
    /**
     * Creates an IOSLocationPlatformDataSource instance.
     */
    actual fun create(): LocationPlatformDataSource {
        return IOSLocationPlatformDataSource()
    }
}
