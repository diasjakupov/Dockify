package io.diasjakupov.dockify.core.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import sp.bvantur.inspektify.ktor.InspektifyKtor

/**
 * Android implementation of HttpClientFactory using OkHttp engine.
 */
actual class HttpClientFactory {
    actual fun create(): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(defaultJson)
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("Ktor", message)
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
