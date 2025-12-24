package io.diasjakupov.dockify.features.auth.domain.usecase

import io.diasjakupov.dockify.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing authentication state changes.
 */
class ObserveAuthStateUseCase(
    private val authRepository: AuthRepository
) {

    /**
     * Observes the authentication state.
     *
     * @return Flow emitting true when authenticated, false otherwise
     */
    operator fun invoke(): Flow<Boolean> {
        return authRepository.observeAuthState()
    }
}
