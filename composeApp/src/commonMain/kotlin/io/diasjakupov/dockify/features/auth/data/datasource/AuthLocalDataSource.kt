package io.diasjakupov.dockify.features.auth.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.data.dto.LoginResponseDto
import io.diasjakupov.dockify.features.auth.data.dto.UserResponseDto
import kotlinx.coroutines.flow.Flow

/**
 * Local data source interface for storing authentication data.
 * Typically implemented using DataStore.
 */
interface AuthLocalDataSource {

    /**
     * Saves user data to local storage.
     *
     * @param user The user data to save
     * @return EmptyResult indicating success or a local storage error
     */
    suspend fun saveUser(user: LoginResponseDto): EmptyResult<DataError>

    /**
     * Retrieves user data from local storage.
     *
     * @return Resource containing the user data or an error if not found
     */
    suspend fun getUser(): Resource<UserResponseDto, DataError>

    /**
     * Clears user data from local storage (logout).
     *
     * @return EmptyResult indicating success or a local storage error
     */
    suspend fun clearUser(): EmptyResult<DataError>

    /**
     * Observes authentication state changes.
     *
     * @return Flow emitting true when user data exists, false otherwise
     */
    fun observeAuthState(): Flow<Boolean>

    /**
     * Checks if user data exists in local storage.
     *
     * @return true if user data exists, false otherwise
     */
    suspend fun hasUser(): Boolean
}
