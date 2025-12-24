package io.diasjakupov.dockify.features.auth.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.domain.model.RegistrationData
import io.diasjakupov.dockify.features.auth.domain.repository.AuthRepository

private const val TAG = "RegisterUseCase"

/**
 * Use case for user registration.
 * Validates registration data and delegates to the repository.
 */
class RegisterUseCase(
    private val authRepository: AuthRepository
) {

    /**
     * Executes the registration operation.
     *
     * @param email User's email address
     * @param password User's password
     * @param username User's username
     * @param firstName User's first name
     * @param lastName User's last name
     * @return Resource containing the created user ID or an error
     */
    suspend operator fun invoke(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String
    ): Resource<String, DataError> {
        println("$TAG: invoke called - email: $email, username: $username")

        // Validate input
        if (email.isBlank() || !isValidEmail(email)) {
            println("$TAG: Invalid email")
            return Resource.Error(DataError.Auth.INVALID_CREDENTIALS)
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            println("$TAG: Password too short (min: $MIN_PASSWORD_LENGTH)")
            return Resource.Error(DataError.Auth.INVALID_CREDENTIALS)
        }
        if (username.isBlank()) {
            println("$TAG: Username is blank")
            return Resource.Error(DataError.Auth.INVALID_CREDENTIALS)
        }

        val registrationData = RegistrationData(
            email = email.trim(),
            password = password,
            username = username.trim(),
            firstName = firstName.trim(),
            lastName = lastName.trim()
        )

        println("$TAG: Calling authRepository.register")
        return authRepository.register(registrationData).also { result ->
            println("$TAG: authRepository.register returned: $result")
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }
}
