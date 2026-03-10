package io.diasjakupov.dockify.features.auth.presentation.profile

import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val profilePresentationModule = module {
    viewModelOf(::ProfileViewModel)
}
