package io.diasjakupov.dockify.features.location.presentation.nearby

import io.diasjakupov.dockify.features.location.domain.model.Hospital
import io.diasjakupov.dockify.ui.base.UiAction

sealed interface NearbyAction : UiAction {
    data object CheckPermissionAndLoadData : NearbyAction
    data object RequestPermission : NearbyAction
    data object PermissionGranted : NearbyAction
    data object PermissionDenied : NearbyAction
    data object RefreshNearbyUsers : NearbyAction
    data object DismissError : NearbyAction
    data object OpenLocationSettings : NearbyAction
    data object RetryLastAction : NearbyAction
    data class SelectTab(val tab: NearbyTab) : NearbyAction
    data class OpenDirections(val hospital: Hospital) : NearbyAction
}
