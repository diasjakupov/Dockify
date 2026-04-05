package io.diasjakupov.dockify.features.chat.presentation

import io.diasjakupov.dockify.ui.base.UiEffect

sealed interface ChatEffect : UiEffect {
    data class ShowSnackbar(val message: String) : ChatEffect
}
