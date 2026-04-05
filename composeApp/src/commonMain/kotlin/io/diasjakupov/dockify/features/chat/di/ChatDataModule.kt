package io.diasjakupov.dockify.features.chat.di

import io.diasjakupov.dockify.features.chat.data.datasource.ChatRemoteDataSource
import io.diasjakupov.dockify.features.chat.data.datasource.ChatRemoteDataSourceImpl
import io.diasjakupov.dockify.features.chat.data.repository.ChatRepositoryImpl
import io.diasjakupov.dockify.features.chat.domain.repository.ChatRepository
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val chatDataModule: Module = module {

    single<ChatRemoteDataSource> {
        ChatRemoteDataSourceImpl(
            httpClient = get(),
            streamingHttpClient = get(named("streaming")),
            baseUrl = get(named("baseUrl"))
        )
    }

    single<ChatRepository> {
        ChatRepositoryImpl(remoteDataSource = get())
    }
}
