package io.diasjakupov.dockify.features.auth.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.features.auth.domain.repository.AuthRepository

/**
 * Use case for user logout.
 * Clears the user session and local data.
 */
class LogoutUseCase(
    private val authRepository: AuthRepository
) {

    /**
     * Executes the logout operation.
     *
     * @return EmptyResult indicating success or an error
     */
    suspend operator fun invoke(): EmptyResult<DataError> {
        return authRepository.logout()
    }
}
