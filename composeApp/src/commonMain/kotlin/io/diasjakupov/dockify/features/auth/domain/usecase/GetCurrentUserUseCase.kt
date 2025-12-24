package io.diasjakupov.dockify.features.auth.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.domain.model.User
import io.diasjakupov.dockify.features.auth.domain.repository.AuthRepository

/**
 * Use case for retrieving the current authenticated user.
 */
class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {

    /**
     * Retrieves the currently authenticated user.
     *
     * @return Resource containing the User or an error if not authenticated
     */
    suspend operator fun invoke(): Resource<User, DataError> {
        return authRepository.getCurrentUser()
    }
}
