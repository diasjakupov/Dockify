package io.diasjakupov.dockify.features.chat.presentation

import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.chat.domain.model.ChatMessage
import io.diasjakupov.dockify.features.chat.domain.usecase.ClearChatHistoryUseCase
import io.diasjakupov.dockify.features.chat.domain.usecase.GetChatHistoryUseCase
import io.diasjakupov.dockify.features.chat.domain.usecase.SendMessageUseCase
import io.diasjakupov.dockify.ui.base.BaseViewModel
import io.diasjakupov.dockify.ui.base.LoadingState
import io.diasjakupov.dockify.ui.base.toUserMessage

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getChatHistoryUseCase: GetChatHistoryUseCase,
    private val clearChatHistoryUseCase: ClearChatHistoryUseCase,
    val docId: String?,
    val documentName: String?
) : BaseViewModel<ChatState, ChatAction, ChatEffect>(
    ChatState(docId = docId, documentName = documentName)
) {

    override fun handleAction(action: ChatAction) {
        when (action) {
            is ChatAction.LoadHistory -> loadHistory()
            is ChatAction.UpdateInput -> updateState { copy(inputText = action.text) }
            is ChatAction.SendMessage -> sendMessage(action.text)
            is ChatAction.ClearHistory -> clearHistory()
            is ChatAction.RetryLoad -> loadHistory()
            is ChatAction.ErrorDismissed -> updateState { copy(error = null) }
        }
    }

    private fun loadHistory() {
        updateState { copy(loadingState = LoadingState.LOADING) }
        launch {
            when (val result = getChatHistoryUseCase(docId)) {
                is Resource.Success -> updateState {
                    copy(messages = result.data, loadingState = LoadingState.IDLE)
                }
                is Resource.Error -> {
                    val message = result.error.toUserMessage()
                    updateState { copy(loadingState = LoadingState.IDLE, error = message) }
                }
            }
        }
    }

    private fun sendMessage(text: String) {
        if (text.isBlank() || currentState.isStreaming) return

        val userMessage = ChatMessage(
            id = -1,
            userId = 0,
            docId = docId,
            role = "user",
            content = text,
            createdAt = ""
        )
        val assistantPlaceholder = ChatMessage(
            id = -2,
            userId = 0,
            docId = docId,
            role = "assistant",
            content = "",
            createdAt = ""
        )

        updateState {
            copy(
                messages = messages + userMessage + assistantPlaceholder,
                inputText = "",
                isStreaming = true
            )
        }

        launch(
            onError = {
                updateState { copy(isStreaming = false) }
                emitEffect(ChatEffect.ShowSnackbar("Connection lost. Try again."))
            }
        ) {
            val flow = sendMessageUseCase(docId, text)
            flow.collect { chunk ->
                updateState {
                    val updated = messages.toMutableList()
                    val lastIndex = updated.lastIndex
                    if (lastIndex >= 0 && updated[lastIndex].role == "assistant") {
                        updated[lastIndex] = updated[lastIndex].copy(
                            content = updated[lastIndex].content + chunk
                        )
                    }
                    copy(messages = updated)
                }
            }
            updateState { copy(isStreaming = false) }
        }
    }

    private fun clearHistory() {
        launch {
            when (val result = clearChatHistoryUseCase(docId)) {
                is Resource.Success -> updateState { copy(messages = emptyList()) }
                is Resource.Error -> {
                    emitEffect(ChatEffect.ShowSnackbar(result.error.toUserMessage()))
                }
            }
        }
    }
}
