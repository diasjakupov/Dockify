package io.diasjakupov.dockify.features.auth.presentation.login

import io.diasjakupov.dockify.ui.base.UiEffect

sealed interface LoginEffect : UiEffect {
    data object NavigateToHome : LoginEffect
    data object NavigateToRegister : LoginEffect
    data object NavigateToForgotPassword : LoginEffect
    data class ShowSnackbar(val message: String) : LoginEffect
}
