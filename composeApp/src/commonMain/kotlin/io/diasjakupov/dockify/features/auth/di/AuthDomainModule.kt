package io.diasjakupov.dockify.features.auth.di

import io.diasjakupov.dockify.features.auth.domain.usecase.GetCurrentUserUseCase
import io.diasjakupov.dockify.features.auth.domain.usecase.LoginUseCase
import io.diasjakupov.dockify.features.auth.domain.usecase.LogoutUseCase
import io.diasjakupov.dockify.features.auth.domain.usecase.ObserveAuthStateUseCase
import io.diasjakupov.dockify.features.auth.domain.usecase.RegisterUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val authDomainModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::GetCurrentUserUseCase)
    factoryOf(::ObserveAuthStateUseCase)
}
