package io.diasjakupov.dockify.features.location.presentation.nearby

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import io.diasjakupov.dockify.features.location.domain.model.Location
import io.diasjakupov.dockify.features.location.domain.model.NearbyUser

private val londonLatLng = LatLng(51.5074, -0.1278) // Fallback when user location unavailable

@Composable
actual fun MapView(
    userLocation: Location?,
    nearbyUsers: List<NearbyUser>,
    modifier: Modifier
) {
    val center = userLocation?.let { LatLng(it.latitude, it.longitude) } ?: londonLatLng

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 14f)
    }

    LaunchedEffect(userLocation) {
        userLocation?.let { loc ->
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                    LatLng(loc.latitude, loc.longitude), 14f
                )
            )
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        // Self marker — blue
        userLocation?.let {
            Marker(
                state = remember(it.latitude, it.longitude) {
                    MarkerState(position = LatLng(it.latitude, it.longitude))
                },
                title = "You",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )
        }

        // Nearby user markers — orange
        nearbyUsers.forEach { user ->
            Marker(
                state = remember(user.userId) {
                    MarkerState(position = LatLng(user.location.latitude, user.location.longitude))
                },
                title = "User ${user.userId.take(6)}",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            )
        }
    }
}
