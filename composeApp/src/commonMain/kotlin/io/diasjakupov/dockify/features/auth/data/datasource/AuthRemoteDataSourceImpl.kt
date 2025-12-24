package io.diasjakupov.dockify.features.auth.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.core.network.safeApiCall
import io.diasjakupov.dockify.features.auth.data.dto.CreatedUserResponseDto
import io.diasjakupov.dockify.features.auth.data.dto.LoginResponseDto
import io.diasjakupov.dockify.features.auth.data.dto.UserLoginRequestDto
import io.diasjakupov.dockify.features.auth.data.dto.UserRegisterRequestDto
import io.diasjakupov.dockify.features.auth.data.dto.UserResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody

private const val TAG = "AuthRemoteDataSource"

class AuthRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : AuthRemoteDataSource {

    override suspend fun login(request: UserLoginRequestDto): Resource<LoginResponseDto, DataError> {
        println("$TAG: Login request - email: ${request.email}")
        val url = "$baseUrl/api/v1/login"
        println("$TAG: POST $url")
        val result: Resource<LoginResponseDto, DataError> = safeApiCall {
            httpClient.post(url) {
                setBody(request)
            }
        }
        println("$TAG: Login result: $result")
        return result
    }

    override suspend fun register(request: UserRegisterRequestDto): Resource<CreatedUserResponseDto, DataError> {
        println("$TAG: Register request - email: ${request.email}, username: ${request.username}")
        val url = "$baseUrl/api/v1/register"
        println("$TAG: POST $url")
        val result: Resource<CreatedUserResponseDto, DataError> = safeApiCall {
            httpClient.post(url) {
                setBody(request)
            }
        }
        println("$TAG: Register result: $result")
        return result
    }
}
