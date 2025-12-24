package io.diasjakupov.dockify.features.location.presentation.nearby

import io.diasjakupov.dockify.features.location.domain.model.Location
import io.diasjakupov.dockify.features.location.domain.model.NearbyUser
import io.diasjakupov.dockify.ui.base.LoadingState
import io.diasjakupov.dockify.ui.base.UiState
import io.diasjakupov.dockify.ui.base.WithError
import io.diasjakupov.dockify.ui.base.WithLoading

/**
 * Permission state for location access.
 */
enum class LocationPermissionState {
    Unknown,
    Granted,
    Denied,
    GpsDisabled
}

/**
 * UI state for the Nearby screen.
 */
data class NearbyState(
    val currentLocation: Location? = null,
    val nearbyUsers: List<NearbyUser> = emptyList(),
    val permissionState: LocationPermissionState = LocationPermissionState.Unknown,
    val isManualRefreshing: Boolean = false,
    val hasInitiallyLoaded: Boolean = false,
    override val loadingState: LoadingState = LoadingState.IDLE,
    override val error: String? = null
) : UiState, WithLoading, WithError {

    val hasNearbyUsers: Boolean
        get() = nearbyUsers.isNotEmpty()

    val needsPermission: Boolean
        get() = permissionState == LocationPermissionState.Unknown ||
                permissionState == LocationPermissionState.Denied

    val isGpsDisabled: Boolean
        get() = permissionState == LocationPermissionState.GpsDisabled

    val canRefresh: Boolean
        get() = !isManualRefreshing && !isLoading && permissionState == LocationPermissionState.Granted
}
