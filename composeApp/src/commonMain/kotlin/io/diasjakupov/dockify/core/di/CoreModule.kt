package io.diasjakupov.dockify.core.di

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

    // Base URL for API calls (should be configured per environment)
    single(named("baseUrl")) {
        "https://aokhan.com" // TODO: Replace with actual API URL
    }

    // HttpClient factory
    single {
        HttpClientFactory()
    }

    // HttpClient instance
    single<HttpClient> {
        get<HttpClientFactory>().create()
    }
}
