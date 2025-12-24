package io.diasjakupov.dockify.features.location.presentation.nearby

import io.diasjakupov.dockify.ui.base.UiEffect

/**
 * One-time effects emitted by the Nearby ViewModel.
 */
sealed interface NearbyEffect : UiEffect {
    /** Show a snackbar message */
    data class ShowSnackbar(val message: String) : NearbyEffect

    /** Open device GPS/location settings */
    data object OpenGpsSettings : NearbyEffect

    /** Notify that location was successfully fetched */
    data object LocationFetched : NearbyEffect
}
