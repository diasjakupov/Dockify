package io.diasjakupov.dockify.features.auth.presentation.register

import io.diasjakupov.dockify.ui.base.UiEffect

sealed interface RegisterEffect : UiEffect {
    data object NavigateToLogin : RegisterEffect
    data class ShowSnackbar(val message: String) : RegisterEffect
    data class ShowSuccessMessage(val message: String) : RegisterEffect
}
