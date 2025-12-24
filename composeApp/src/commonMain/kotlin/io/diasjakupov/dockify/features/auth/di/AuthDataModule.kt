package io.diasjakupov.dockify.features.auth.di

import io.diasjakupov.dockify.features.auth.data.datasource.AuthLocalDataSource
import io.diasjakupov.dockify.features.auth.data.datasource.AuthLocalDataSourceImpl
import io.diasjakupov.dockify.features.auth.data.datasource.AuthRemoteDataSource
import io.diasjakupov.dockify.features.auth.data.datasource.AuthRemoteDataSourceImpl
import io.diasjakupov.dockify.features.auth.data.repository.AuthRepositoryImpl
import io.diasjakupov.dockify.features.auth.domain.repository.AuthRepository
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val authDataModule: Module = module {

    // Local Data Source (uses DataStore provided by platform module)
    single<AuthLocalDataSource> {
        AuthLocalDataSourceImpl(
            dataStore = get()
        )
    }

    // Remote Data Source
    single<AuthRemoteDataSource> {
        AuthRemoteDataSourceImpl(
            httpClient = get(),
            baseUrl = get(named("baseUrl"))
        )
    }

    // Repository
    single<AuthRepository> {
        AuthRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get()
        )
    }
}
