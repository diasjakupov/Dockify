package io.diasjakupov.dockify.features.location.presentation.nearby

import io.diasjakupov.dockify.ui.base.UiAction

/**
 * Actions that can be triggered from the Nearby screen.
 */
sealed interface NearbyAction : UiAction {
    /** Check permissions and load nearby users data */
    data object CheckPermissionAndLoadData : NearbyAction

    /** Request location permissions from the platform */
    data object RequestPermission : NearbyAction

    /** Called when permissions are granted */
    data object PermissionGranted : NearbyAction

    /** Called when permissions are denied */
    data object PermissionDenied : NearbyAction

    /** Refresh nearby users list */
    data object RefreshNearbyUsers : NearbyAction

    /** Dismiss the current error */
    data object DismissError : NearbyAction

    /** Open device location settings */
    data object OpenLocationSettings : NearbyAction

    /** Retry the last failed action */
    data object RetryLastAction : NearbyAction
}
