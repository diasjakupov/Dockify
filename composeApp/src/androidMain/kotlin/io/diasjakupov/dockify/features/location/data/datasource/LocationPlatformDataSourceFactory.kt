package io.diasjakupov.dockify.features.location.data.datasource

import android.content.Context

/**
 * Android actual implementation of LocationPlatformDataSourceFactory.
 * Creates AndroidLocationPlatformDataSource using FusedLocationProvider.
 */
actual class LocationPlatformDataSourceFactory(
    private val context: Context
) {
    /**
     * Creates an AndroidLocationPlatformDataSource instance.
     */
    actual fun create(): LocationPlatformDataSource {
        return AndroidLocationPlatformDataSource(context)
    }
}
