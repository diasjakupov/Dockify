package io.diasjakupov.dockify.features.chat.presentation.di

import io.diasjakupov.dockify.features.chat.presentation.ChatViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val chatPresentationModule: Module = module {
    viewModel { params ->
        ChatViewModel(
            sendMessageUseCase = get(),
            getChatHistoryUseCase = get(),
            clearChatHistoryUseCase = get(),
            docId = params.getOrNull(),
            documentName = params.getOrNull()
        )
    }
}
