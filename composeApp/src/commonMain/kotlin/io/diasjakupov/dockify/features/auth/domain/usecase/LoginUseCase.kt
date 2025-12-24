package io.diasjakupov.dockify.features.auth.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.domain.model.AuthCredentials
import io.diasjakupov.dockify.features.auth.domain.model.User
import io.diasjakupov.dockify.features.auth.domain.repository.AuthRepository

/**
 * Use case for user login.
 * Validates credentials and delegates to the repository.
 */
class LoginUseCase(
    private val authRepository: AuthRepository
) {

    /**
     * Executes the login operation.
     *
     * @param email User's email address
     * @param password User's password
     * @return Resource containing the authenticated User or an error
     */
    suspend operator fun invoke(email: String, password: String): Resource<User, DataError> {
        // Validate input
        if (email.isBlank()) {
            return Resource.Error(DataError.Auth.INVALID_CREDENTIALS)
        }
        if (password.isBlank()) {
            return Resource.Error(DataError.Auth.INVALID_CREDENTIALS)
        }

        val credentials = AuthCredentials(
            email = email.trim(),
            password = password
        )

        return authRepository.login(credentials)
    }
}
