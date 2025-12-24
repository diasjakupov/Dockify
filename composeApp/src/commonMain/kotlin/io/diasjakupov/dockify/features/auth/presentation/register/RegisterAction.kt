package io.diasjakupov.dockify.features.auth.presentation.register

import io.diasjakupov.dockify.ui.base.UiAction

sealed interface RegisterAction : UiAction {
    data class EmailChanged(val email: String) : RegisterAction
    data class PasswordChanged(val password: String) : RegisterAction
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterAction
    data class UsernameChanged(val username: String) : RegisterAction
    data class FirstNameChanged(val firstName: String) : RegisterAction
    data class LastNameChanged(val lastName: String) : RegisterAction
    data object RegisterClicked : RegisterAction
    data object LoginClicked : RegisterAction
    data object ErrorDismissed : RegisterAction
}
