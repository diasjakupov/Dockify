package io.diasjakupov.dockify.features.auth.presentation.login

import io.diasjakupov.dockify.ui.base.LoadingState
import io.diasjakupov.dockify.ui.base.UiState
import io.diasjakupov.dockify.ui.base.WithError
import io.diasjakupov.dockify.ui.base.WithLoading

data class LoginState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    override val loadingState: LoadingState = LoadingState.IDLE,
    override val error: String? = null
) : UiState, WithLoading, WithError {

    val isLoginEnabled: Boolean
        get() = email.isNotBlank() &&
                password.isNotBlank() &&
                emailError == null &&
                passwordError == null &&
                !isLoading
}
