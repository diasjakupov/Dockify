package io.diasjakupov.dockify.features.location.di

import io.diasjakupov.dockify.features.location.data.datasource.LocationPlatformDataSourceFactory
import io.diasjakupov.dockify.features.location.permission.LocationPermissionHandler
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific Koin module for Location feature.
 * Provides the LocationPlatformDataSourceFactory and LocationPermissionHandler.
 */
val iosLocationModule: Module = module {
    single {
        LocationPlatformDataSourceFactory()
    }

    single {
        LocationPermissionHandler()
    }
}
