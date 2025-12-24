package io.diasjakupov.dockify.features.auth.presentation.login

import io.diasjakupov.dockify.ui.base.UiAction

sealed interface LoginAction : UiAction {
    data class EmailChanged(val email: String) : LoginAction
    data class PasswordChanged(val password: String) : LoginAction
    data object LoginClicked : LoginAction
    data object RegisterClicked : LoginAction
    data object ForgotPasswordClicked : LoginAction
    data object ErrorDismissed : LoginAction
}
