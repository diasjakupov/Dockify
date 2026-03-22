package io.diasjakupov.dockify.features.location.presentation.nearby

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.diasjakupov.dockify.features.location.domain.model.Location
import io.diasjakupov.dockify.features.location.domain.model.NearbyUser

/**
 * Cross-platform map view showing the user's current location and nearby users.
 * Android: Google Maps Compose.  iOS: MapKit via UIKitView.
 */
@Composable
expect fun MapView(
    userLocation: Location?,
    nearbyUsers: List<NearbyUser>,
    modifier: Modifier = Modifier
)
