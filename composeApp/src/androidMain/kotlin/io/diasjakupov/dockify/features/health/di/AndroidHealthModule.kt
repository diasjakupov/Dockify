package io.diasjakupov.dockify.features.health.di

import io.diasjakupov.dockify.features.health.data.datasource.HealthPlatformDataSourceFactory
import io.diasjakupov.dockify.features.health.permission.HealthPermissionHandler
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific Koin module for Health feature.
 * Provides the HealthPlatformDataSourceFactory and HealthPermissionHandler with Android context.
 */
val androidHealthModule: Module = module {
    single {
        HealthPlatformDataSourceFactory(androidContext())
    }

    single {
        HealthPermissionHandler(androidContext())
    }
}
