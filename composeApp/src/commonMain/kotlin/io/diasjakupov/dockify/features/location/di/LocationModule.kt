package io.diasjakupov.dockify.features.location.di

import io.diasjakupov.dockify.features.location.data.datasource.LocationPlatformDataSource
import io.diasjakupov.dockify.features.location.data.datasource.LocationPlatformDataSourceFactory
import io.diasjakupov.dockify.features.location.data.datasource.LocationRemoteDataSource
import io.diasjakupov.dockify.features.location.data.datasource.LocationRemoteDataSourceImpl
import io.diasjakupov.dockify.features.location.data.repository.LocationRepositoryImpl
import io.diasjakupov.dockify.features.location.domain.repository.LocationRepository
import io.diasjakupov.dockify.features.location.domain.usecase.GetCurrentLocationUseCase
import io.diasjakupov.dockify.features.location.domain.usecase.GetNearestHospitalsUseCase
import io.diasjakupov.dockify.features.location.domain.usecase.GetNearestUsersUseCase
import io.diasjakupov.dockify.features.location.domain.usecase.RequestLocationPermissionUseCase
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin dependency injection module for the Location feature.
 *
 * Note: Platform-specific LocationPlatformDataSourceFactory must be provided
 * by platform-specific modules (androidLocationModule, iosLocationModule).
 */
val locationModule: Module = module {

    // Platform Data Source (provided by factory)
    single<LocationPlatformDataSource> {
        get<LocationPlatformDataSourceFactory>().create()
    }

    // Remote Data Source
    single<LocationRemoteDataSource> {
        LocationRemoteDataSourceImpl(
            httpClient = get(),
            baseUrl = get(named("baseUrl"))
        )
    }

    // Repository
    single<LocationRepository> {
        LocationRepositoryImpl(
            platformDataSource = get(),
            remoteDataSource = get()
        )
    }

    // Use Cases
    factory {
        GetCurrentLocationUseCase(
            locationRepository = get()
        )
    }

    factory {
        GetNearestUsersUseCase(
            locationRepository = get()
        )
    }

    factory {
        GetNearestHospitalsUseCase(
            locationRepository = get()
        )
    }

    factory {
        RequestLocationPermissionUseCase(
            locationRepository = get()
        )
    }
}
