package io.diasjakupov.dockify.features.health.di

import io.diasjakupov.dockify.features.health.data.datasource.HealthPlatformDataSourceFactory
import io.diasjakupov.dockify.features.health.permission.HealthPermissionHandler
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific Koin module for Health feature.
 * Provides the HealthPlatformDataSourceFactory and HealthPermissionHandler for iOS.
 */
val iosHealthModule: Module = module {
    single {
        HealthPlatformDataSourceFactory()
    }

    single {
        HealthPermissionHandler()
    }
}
