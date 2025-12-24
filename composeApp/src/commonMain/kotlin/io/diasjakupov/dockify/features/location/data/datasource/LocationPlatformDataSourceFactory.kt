package io.diasjakupov.dockify.features.location.data.datasource

/**
 * Factory for creating platform-specific LocationPlatformDataSource instances.
 * Uses expect/actual pattern for KMP.
 */
expect class LocationPlatformDataSourceFactory {
    /**
     * Creates a platform-specific LocationPlatformDataSource implementation.
     */
    fun create(): LocationPlatformDataSource
}
