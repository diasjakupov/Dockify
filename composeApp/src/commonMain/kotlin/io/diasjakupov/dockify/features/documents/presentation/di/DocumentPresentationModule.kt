package io.diasjakupov.dockify.features.documents.presentation.di

import io.diasjakupov.dockify.features.documents.presentation.documents.DocumentsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val documentPresentationModule: Module = module {
    viewModelOf(::DocumentsViewModel)
}
