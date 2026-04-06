package io.diasjakupov.dockify.core.di

import io.diasjakupov.dockify.core.demo.DemoModeRepository
import io.diasjakupov.dockify.core.demo.DemoModeRepositoryImpl
import io.diasjakupov.dockify.core.network.HttpClientFactory
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Core dependency injection module.
 * Provides application-wide dependencies like HttpClient and configuration.
 */
val coreModule: Module = module {
    single(named("baseUrl")) {
        "https://aokhan.com"
    }
    single {
        HttpClientFactory()
    }
    single<HttpClient> {
        get<HttpClientFactory>().create()
    }
    single<HttpClient>(named("streaming")) {
        get<HttpClientFactory>().createStreaming()
    }
    single<DemoModeRepository> { DemoModeRepositoryImpl(dataStore = get()) }
}
