package io.diasjakupov.dockify.features.location.presentation.di

import io.diasjakupov.dockify.features.location.presentation.nearby.NearbyViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for Location presentation layer.
 */
val locationPresentationModule: Module = module {
    viewModel {
        NearbyViewModel(
            getCurrentLocationUseCase = get(),
            getNearestUsersUseCase = get(),
            getCurrentUserUseCase = get(),
            locationRepository = get(),
            permissionHandler = get()
        )
    }
}
