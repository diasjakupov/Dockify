package io.diasjakupov.dockify.features.auth.domain.repository

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.domain.model.AuthCredentials
import io.diasjakupov.dockify.features.auth.domain.model.RegistrationData
import io.diasjakupov.dockify.features.auth.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 * Domain layer defines this contract; data layer provides implementation.
 */
interface AuthRepository {

    /**
     * Authenticates a user with the provided credentials.
     *
     * @param credentials The user's login credentials (email and password)
     * @return Resource containing the authenticated User or an error
     */
    suspend fun login(credentials: AuthCredentials): Resource<User, DataError>

    /**
     * Registers a new user with the provided registration data.
     *
     * @param data The registration information
     * @return Resource containing the created user ID or an error
     */
    suspend fun register(data: RegistrationData): Resource<String, DataError>

    /**
     * Logs out the current user and clears the session.
     *
     * @return EmptyResult indicating success or an error
     */
    suspend fun logout(): EmptyResult<DataError>

    /**
     * Retrieves the currently authenticated user from local storage.
     *
     * @return Resource containing the User or an error if not authenticated
     */
    suspend fun getCurrentUser(): Resource<User, DataError>

    /**
     * Observes the authentication state as a Flow.
     *
     * @return Flow emitting true when authenticated, false otherwise
     */
    fun observeAuthState(): Flow<Boolean>

    /**
     * Checks if a user is currently authenticated.
     *
     * @return true if authenticated, false otherwise
     */
    suspend fun isAuthenticated(): Boolean
}
