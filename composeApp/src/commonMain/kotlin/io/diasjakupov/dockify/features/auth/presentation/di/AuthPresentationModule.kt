package io.diasjakupov.dockify.features.auth.presentation.di

import io.diasjakupov.dockify.features.auth.presentation.login.LoginViewModel
import io.diasjakupov.dockify.features.auth.presentation.register.RegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authPresentationModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
}
