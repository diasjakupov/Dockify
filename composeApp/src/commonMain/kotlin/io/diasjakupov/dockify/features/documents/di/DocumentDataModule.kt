package io.diasjakupov.dockify.features.documents.di

import io.diasjakupov.dockify.features.documents.data.datasource.DocumentRemoteDataSource
import io.diasjakupov.dockify.features.documents.data.datasource.DocumentRemoteDataSourceImpl
import io.diasjakupov.dockify.features.documents.data.repository.DocumentRepositoryImpl
import io.diasjakupov.dockify.features.documents.domain.repository.DocumentRepository
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val documentDataModule: Module = module {

    single<DocumentRemoteDataSource> {
        DocumentRemoteDataSourceImpl(
            httpClient = get(),
            baseUrl = get(named("baseUrl"))
        )
    }

    single<DocumentRepository> {
        DocumentRepositoryImpl(remoteDataSource = get())
    }
}
