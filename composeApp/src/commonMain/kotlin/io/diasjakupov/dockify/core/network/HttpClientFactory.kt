package io.diasjakupov.dockify.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory for creating configured HttpClient instances.
 */
expect class HttpClientFactory() {
    fun create(): HttpClient
}

/**
 * Common JSON configuration for serialization.
 */
val defaultJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    prettyPrint = false
}

/**
 * Extension function to configure common HttpClient settings.
 */
fun HttpClient.configureDefaults(baseUrl: String): HttpClient {
    return HttpClient(this.engine) {
        install(ContentNegotiation) {
            json(defaultJson)
        }
        defaultRequest {
            url(baseUrl)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }
}
