package io.diasjakupov.dockify.features.auth.data.repository

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.core.domain.map
import io.diasjakupov.dockify.features.auth.data.datasource.AuthLocalDataSource
import io.diasjakupov.dockify.features.auth.data.datasource.AuthRemoteDataSource
import io.diasjakupov.dockify.features.auth.data.mapper.UserMapper.toDto
import io.diasjakupov.dockify.features.auth.data.mapper.UserMapper.toDomain
import io.diasjakupov.dockify.features.auth.domain.model.AuthCredentials
import io.diasjakupov.dockify.features.auth.domain.model.RegistrationData
import io.diasjakupov.dockify.features.auth.domain.model.User
import io.diasjakupov.dockify.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

private const val TAG = "AuthRepositoryImpl"

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : AuthRepository {

    override suspend fun login(credentials: AuthCredentials): Resource<User, DataError> {
        println("$TAG: login called - email: ${credentials.email}")
        return when (val result = remoteDataSource.login(credentials.toDto())) {
            is Resource.Success -> {
                println("$TAG: login success, saving user locally")
                // Save user and tokens locally
                localDataSource.saveUser(result.data)
                Resource.Success(result.data.user.toDomain())
            }
            is Resource.Error -> {
                println("$TAG: login error: ${result.error}")
                result
            }
        }
    }

    override suspend fun register(data: RegistrationData): Resource<String, DataError> {
        println("$TAG: register called - email: ${data.email}, username: ${data.username}")
        return when (val result = remoteDataSource.register(data.toDto())) {
            is Resource.Success -> {
                println("$TAG: register success - userId: ${result.data.userId}")
                Resource.Success(result.data.userId.toString())
            }
            is Resource.Error -> {
                println("$TAG: register error: ${result.error}")
                result
            }
        }
    }

    override suspend fun logout(): EmptyResult<DataError> {
        return localDataSource.clearUser()
    }

    override suspend fun getCurrentUser(): Resource<User, DataError> {
        return localDataSource.getUser().map { it.toDomain() }
    }

    override fun observeAuthState(): Flow<Boolean> {
        return localDataSource.observeAuthState()
    }

    override suspend fun isAuthenticated(): Boolean {
        return localDataSource.hasUser()
    }
}
