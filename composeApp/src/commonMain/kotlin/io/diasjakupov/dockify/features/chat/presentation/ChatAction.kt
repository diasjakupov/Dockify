package io.diasjakupov.dockify.features.chat.presentation

import io.diasjakupov.dockify.ui.base.UiAction

sealed interface ChatAction : UiAction {
    data object LoadHistory : ChatAction
    data class UpdateInput(val text: String) : ChatAction
    data class SendMessage(val text: String) : ChatAction
    data object ClearHistory : ChatAction
    data object RetryLoad : ChatAction
    data object ErrorDismissed : ChatAction
}
