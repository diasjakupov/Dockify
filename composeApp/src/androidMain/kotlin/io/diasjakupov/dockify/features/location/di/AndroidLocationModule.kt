package io.diasjakupov.dockify.features.location.di

import io.diasjakupov.dockify.features.location.data.datasource.LocationPlatformDataSourceFactory
import io.diasjakupov.dockify.features.location.permission.LocationPermissionHandler
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific Koin module for Location feature.
 * Provides the LocationPlatformDataSourceFactory and LocationPermissionHandler with Android context.
 */
val androidLocationModule: Module = module {
    single {
        LocationPlatformDataSourceFactory(androidContext())
    }

    single {
        LocationPermissionHandler(androidContext())
    }
}
