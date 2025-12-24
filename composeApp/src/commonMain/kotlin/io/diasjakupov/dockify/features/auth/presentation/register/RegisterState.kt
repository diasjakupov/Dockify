package io.diasjakupov.dockify.features.auth.presentation.register

import io.diasjakupov.dockify.ui.base.LoadingState
import io.diasjakupov.dockify.ui.base.UiState
import io.diasjakupov.dockify.ui.base.WithError
import io.diasjakupov.dockify.ui.base.WithLoading

data class RegisterState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val usernameError: String? = null,
    override val loadingState: LoadingState = LoadingState.IDLE,
    override val error: String? = null
) : UiState, WithLoading, WithError {

    val isRegisterEnabled: Boolean
        get() = email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                username.isNotBlank() &&
                emailError == null &&
                passwordError == null &&
                confirmPasswordError == null &&
                usernameError == null &&
                !isLoading

    companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
