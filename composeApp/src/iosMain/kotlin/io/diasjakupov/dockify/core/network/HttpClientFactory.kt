package io.diasjakupov.dockify.core.network

import io.github.bvantur.inspektify.ktor.InspektifyKtor
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

/**
 * iOS implementation of HttpClientFactory using Darwin engine.
 */
actual class HttpClientFactory {
    actual fun create(): HttpClient {
        return HttpClient(Darwin) {
            install(ContentNegotiation) {
                json(defaultJson)
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("Ktor: $message")
                    }
                }
                level = LogLevel.ALL
            }
            install(InspektifyKtor)
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }
}
