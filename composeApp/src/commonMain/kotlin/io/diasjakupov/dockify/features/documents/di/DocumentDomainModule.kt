package io.diasjakupov.dockify.features.documents.di

import io.diasjakupov.dockify.features.documents.domain.usecase.DeleteDocumentUseCase
import io.diasjakupov.dockify.features.documents.domain.usecase.DownloadDocumentUseCase
import io.diasjakupov.dockify.features.documents.domain.usecase.GetDocumentsUseCase
import io.diasjakupov.dockify.features.documents.domain.usecase.UploadDocumentUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val documentDomainModule: Module = module {
    factoryOf(::GetDocumentsUseCase)
    factoryOf(::UploadDocumentUseCase)
    factoryOf(::DeleteDocumentUseCase)
    factoryOf(::DownloadDocumentUseCase)
}
