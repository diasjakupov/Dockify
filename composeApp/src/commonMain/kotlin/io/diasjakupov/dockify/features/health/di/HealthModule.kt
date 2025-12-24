package io.diasjakupov.dockify.features.health.di

import io.diasjakupov.dockify.features.health.data.datasource.HealthLocalDataSource
import io.diasjakupov.dockify.features.health.data.datasource.HealthLocalDataSourceImpl
import io.diasjakupov.dockify.features.health.data.datasource.HealthPlatformDataSource
import io.diasjakupov.dockify.features.health.data.datasource.HealthPlatformDataSourceFactory
import io.diasjakupov.dockify.features.health.data.datasource.HealthRemoteDataSource
import io.diasjakupov.dockify.features.health.data.datasource.HealthRemoteDataSourceImpl
import io.diasjakupov.dockify.features.health.data.repository.HealthRepositoryImpl
import io.diasjakupov.dockify.features.health.domain.repository.HealthRepository
import io.diasjakupov.dockify.features.health.domain.usecase.CheckHealthPermissionsUseCase
import io.diasjakupov.dockify.features.health.domain.usecase.ReadPlatformHealthDataUseCase
import io.diasjakupov.dockify.features.health.domain.usecase.RequestHealthPermissionsUseCase
import io.diasjakupov.dockify.features.health.domain.usecase.SyncHealthDataUseCase
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin dependency injection module for the Health feature.
 *
 * Note: Platform-specific HealthPlatformDataSourceFactory must be provided
 * by platform-specific modules (androidHealthModule, iosHealthModule).
 */
val healthModule: Module = module {

    // Platform Data Source (provided by factory)
    single<HealthPlatformDataSource> {
        get<HealthPlatformDataSourceFactory>().create()
    }

    // Local Data Source
    single<HealthLocalDataSource> {
        HealthLocalDataSourceImpl()
    }

    // Remote Data Source
    single<HealthRemoteDataSource> {
        HealthRemoteDataSourceImpl(
            httpClient = get(),
            baseUrl = get(named("baseUrl"))
        )
    }

    // Repository
    single<HealthRepository> {
        HealthRepositoryImpl(
            platformDataSource = get(),
            remoteDataSource = get(),
            localDataSource = get()
        )
    }

    // Use Cases
    factory {
        SyncHealthDataUseCase(
            healthRepository = get()
        )
    }

    factory {
        ReadPlatformHealthDataUseCase(
            healthRepository = get()
        )
    }

    factory {
        RequestHealthPermissionsUseCase(
            healthRepository = get()
        )
    }

    factory {
        CheckHealthPermissionsUseCase(
            healthRepository = get()
        )
    }
}
