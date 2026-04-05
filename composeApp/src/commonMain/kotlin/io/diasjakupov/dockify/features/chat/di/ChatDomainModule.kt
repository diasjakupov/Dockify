package io.diasjakupov.dockify.features.chat.di

import io.diasjakupov.dockify.features.chat.domain.usecase.ClearChatHistoryUseCase
import io.diasjakupov.dockify.features.chat.domain.usecase.GetChatHistoryUseCase
import io.diasjakupov.dockify.features.chat.domain.usecase.SendMessageUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val chatDomainModule: Module = module {
    factoryOf(::SendMessageUseCase)
    factoryOf(::GetChatHistoryUseCase)
    factoryOf(::ClearChatHistoryUseCase)
}
