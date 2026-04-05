package io.diasjakupov.dockify.di

import io.diasjakupov.dockify.core.di.coreModule
import io.diasjakupov.dockify.features.auth.di.authDataModule
import io.diasjakupov.dockify.features.auth.di.authDomainModule
import io.diasjakupov.dockify.features.auth.presentation.di.authPresentationModule
import io.diasjakupov.dockify.features.auth.presentation.profile.profilePresentationModule
import io.diasjakupov.dockify.features.health.di.healthModule
import io.diasjakupov.dockify.features.health.presentation.di.healthPresentationModule
import io.diasjakupov.dockify.features.location.di.locationModule
import io.diasjakupov.dockify.features.location.presentation.di.locationPresentationModule
import io.diasjakupov.dockify.features.documents.di.documentDataModule
import io.diasjakupov.dockify.features.documents.di.documentDomainModule
import io.diasjakupov.dockify.features.documents.presentation.di.documentPresentationModule
import io.diasjakupov.dockify.features.recommendation.di.recommendationModule
import io.diasjakupov.dockify.features.chat.di.chatDataModule
import io.diasjakupov.dockify.features.chat.di.chatDomainModule
import io.diasjakupov.dockify.features.chat.presentation.di.chatPresentationModule
import org.koin.core.module.Module

/**
 * Provides all common Koin modules for the application.
 * Platform-specific modules should be added in platform initializers.
 */
fun appModules(): List<Module> = listOf(
    coreModule,
    healthModule,
    healthPresentationModule,
    locationModule,
    locationPresentationModule,
    recommendationModule,
    authDataModule,
    authDomainModule,
    authPresentationModule,
    profilePresentationModule,
    documentDataModule,
    documentDomainModule,
    documentPresentationModule,
    chatDataModule,
    chatDomainModule,
    chatPresentationModule
)
