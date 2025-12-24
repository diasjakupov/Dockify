package io.diasjakupov.dockify.core.network

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Safely executes an API call and maps the result to a Resource.
 *
 * @param execute The suspend function that makes the API call
 * @return Resource with either the success data or a mapped DataError
 */
suspend inline fun <reified T> safeApiCall(
    execute: () -> HttpResponse
): Resource<T, DataError> {
    return try {
        val response = execute()
        println("SafeApiCall: Response status: ${response.status}")
        when (response.status) {
            HttpStatusCode.OK,
            HttpStatusCode.Created,
            HttpStatusCode.Accepted -> {
                val body = response.body<T>()
                println("SafeApiCall: Success - parsed body: $body")
                Resource.Success(body)
            }
            HttpStatusCode.Unauthorized -> {
                println("SafeApiCall: Error - Unauthorized (401)")
                Resource.Error(DataError.Auth.UNAUTHORIZED)
            }
            HttpStatusCode.Forbidden -> {
                println("SafeApiCall: Error - Forbidden (403)")
                Resource.Error(DataError.Auth.INVALID_TOKEN)
            }
            HttpStatusCode.NotFound -> {
                println("SafeApiCall: Error - NotFound (404)")
                Resource.Error(DataError.Network.UNKNOWN)
            }
            else -> {
                println("SafeApiCall: Error - Server error (${response.status})")
                Resource.Error(DataError.Network.SERVER_ERROR)
            }
        }
    } catch (e: CancellationException) {
        println("SafeApiCall: Cancelled")
        throw e
    } catch (e: ClientRequestException) {
        println("SafeApiCall: ClientRequestException - status: ${e.response.status}, message: ${e.message}")
        when (e.response.status) {
            HttpStatusCode.Unauthorized -> Resource.Error(DataError.Auth.UNAUTHORIZED)
            HttpStatusCode.Forbidden -> Resource.Error(DataError.Auth.INVALID_TOKEN)
            else -> Resource.Error(DataError.Network.UNKNOWN)
        }
    } catch (e: ServerResponseException) {
        println("SafeApiCall: ServerResponseException - status: ${e.response.status}, message: ${e.message}")
        Resource.Error(DataError.Network.SERVER_ERROR)
    } catch (e: SerializationException) {
        println("SafeApiCall: SerializationException - ${e.message}")
        e.printStackTrace()
        Resource.Error(DataError.Network.SERIALIZATION_ERROR)
    } catch (e: Exception) {
        println("SafeApiCall: Exception - ${e::class.simpleName}: ${e.message}")
        e.printStackTrace()
        Resource.Error(DataError.Network.UNKNOWN)
    }
}

/**
 * Safely executes an API call that returns no body (Unit response).
 */
suspend fun safeApiCallEmpty(
    execute: suspend () -> HttpResponse
): Resource<Unit, DataError> {
    return try {
        val response = execute()
        when (response.status) {
            HttpStatusCode.OK,
            HttpStatusCode.Created,
            HttpStatusCode.Accepted,
            HttpStatusCode.NoContent -> {
                Resource.Success(Unit)
            }
            HttpStatusCode.Unauthorized -> {
                Resource.Error(DataError.Auth.UNAUTHORIZED)
            }
            HttpStatusCode.Forbidden -> {
                Resource.Error(DataError.Auth.INVALID_TOKEN)
            }
            else -> {
                Resource.Error(DataError.Network.SERVER_ERROR)
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: ClientRequestException) {
        when (e.response.status) {
            HttpStatusCode.Unauthorized -> Resource.Error(DataError.Auth.UNAUTHORIZED)
            HttpStatusCode.Forbidden -> Resource.Error(DataError.Auth.INVALID_TOKEN)
            else -> Resource.Error(DataError.Network.UNKNOWN)
        }
    } catch (e: ServerResponseException) {
        Resource.Error(DataError.Network.SERVER_ERROR)
    } catch (e: Exception) {
        Resource.Error(DataError.Network.UNKNOWN)
    }
}
