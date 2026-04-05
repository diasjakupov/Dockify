package io.diasjakupov.dockify.features.location.presentation.nearby

import io.diasjakupov.dockify.ui.base.UiEffect

sealed interface NearbyEffect : UiEffect {
    data class ShowSnackbar(val message: String) : NearbyEffect
    data object OpenGpsSettings : NearbyEffect
    data object LocationFetched : NearbyEffect
    data class OpenDirections(
        val latitude: Double,
        val longitude: Double,
        val label: String
    ) : NearbyEffect
}
