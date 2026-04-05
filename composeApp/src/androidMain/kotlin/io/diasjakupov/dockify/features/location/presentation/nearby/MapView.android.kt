package io.diasjakupov.dockify.features.location.presentation.nearby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import io.diasjakupov.dockify.BuildConfig
import io.diasjakupov.dockify.features.location.domain.model.Location
import io.diasjakupov.dockify.features.location.domain.model.NearbyUser

private val londonLatLng = LatLng(51.5074, -0.1278) // Fallback when user location unavailable

@Composable
actual fun MapView(
    userLocation: Location?,
    nearbyUsers: List<NearbyUser>,
    darkTheme: Boolean,
    modifier: Modifier
) {
    // Show a blank white placeholder when no real Maps API key is configured
    if (BuildConfig.MAPS_API_KEY.isBlank() || BuildConfig.MAPS_API_KEY == "PLACEHOLDER_KEY") {
        Box(modifier = modifier.background(Color.White))
        return
    }

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

    val mapProperties = remember(darkTheme) {
        if (darkTheme) {
            MapProperties(mapStyleOptions = MapStyleOptions(DARK_MAP_STYLE))
        } else {
            MapProperties()
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties
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

/**
 * Google Maps dark style JSON — based on Google's "Night" styling.
 * Darkens the map background, roads, labels, and water to match the app's dark theme.
 */
private const val DARK_MAP_STYLE = """[
  {"elementType":"geometry","stylers":[{"color":"#242f3e"}]},
  {"elementType":"labels.text.fill","stylers":[{"color":"#746855"}]},
  {"elementType":"labels.text.stroke","stylers":[{"color":"#242f3e"}]},
  {"featureType":"administrative.locality","elementType":"labels.text.fill","stylers":[{"color":"#d59563"}]},
  {"featureType":"poi","elementType":"labels.text.fill","stylers":[{"color":"#d59563"}]},
  {"featureType":"poi.park","elementType":"geometry","stylers":[{"color":"#263c3f"}]},
  {"featureType":"poi.park","elementType":"labels.text.fill","stylers":[{"color":"#6b9a76"}]},
  {"featureType":"road","elementType":"geometry","stylers":[{"color":"#38414e"}]},
  {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#212a37"}]},
  {"featureType":"road","elementType":"labels.text.fill","stylers":[{"color":"#9ca5b3"}]},
  {"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#746855"}]},
  {"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#1f2835"}]},
  {"featureType":"road.highway","elementType":"labels.text.fill","stylers":[{"color":"#f3d19c"}]},
  {"featureType":"transit","elementType":"geometry","stylers":[{"color":"#2f3948"}]},
  {"featureType":"transit.station","elementType":"labels.text.fill","stylers":[{"color":"#d59563"}]},
  {"featureType":"water","elementType":"geometry","stylers":[{"color":"#17263c"}]},
  {"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#515c6d"}]},
  {"featureType":"water","elementType":"labels.text.stroke","stylers":[{"color":"#17263c"}]}
]"""
