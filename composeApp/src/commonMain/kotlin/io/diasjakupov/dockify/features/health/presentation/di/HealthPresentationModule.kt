package io.diasjakupov.dockify.features.health.presentation.di

import io.diasjakupov.dockify.features.health.presentation.HealthViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for health presentation layer dependencies.
 */
val healthPresentationModule = module {
    viewModelOf(::HealthViewModel)
}
