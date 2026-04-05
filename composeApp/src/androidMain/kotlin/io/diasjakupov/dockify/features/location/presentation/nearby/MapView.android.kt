package io.diasjakupov.dockify.features.location.presentation.nearby

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptor
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
import io.diasjakupov.dockify.features.location.domain.model.Hospital
import io.diasjakupov.dockify.features.location.domain.model.Location
import io.diasjakupov.dockify.features.location.domain.model.NearbyUser

private val londonLatLng = LatLng(51.5074, -0.1278)

@Composable
actual fun MapView(
    userLocation: Location?,
    nearbyUsers: List<NearbyUser>,
    nearbyHospitals: List<Hospital>,
    onHospitalClick: (Hospital) -> Unit,
    darkTheme: Boolean,
    modifier: Modifier
) {
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

    val selfMarker = remember { createSelfMarkerBitmap() }
    val personMarker = remember { createPersonMarkerBitmap() }
    val hospitalMarker = remember { createHospitalMarkerBitmap() }

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
                icon = selfMarker
            )
        }

        // Nearby user markers — orange
        nearbyUsers.forEach { user ->
            Marker(
                state = remember(user.userId) {
                    MarkerState(position = LatLng(user.location.latitude, user.location.longitude))
                },
                title = "User ${user.userId.take(6)}",
                icon = personMarker
            )
        }

        // Hospital markers — red
        nearbyHospitals.forEachIndexed { index, hospital ->
            val hospitalName = hospital.name ?: generateHospitalName(index)
            Marker(
                state = remember(hospital.location.latitude, hospital.location.longitude) {
                    MarkerState(
                        position = LatLng(
                            hospital.location.latitude,
                            hospital.location.longitude
                        )
                    )
                },
                title = hospitalName,
                snippet = "Tap for directions",
                icon = hospitalMarker,
                onInfoWindowClick = { onHospitalClick(hospital) }
            )
        }
    }
}

private fun createMarkerBitmap(
    backgroundColor: Int,
    drawIcon: (Canvas, Paint) -> Unit
): BitmapDescriptor {
    val size = 96
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Draw circle background
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backgroundColor
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint)

    // Draw icon
    val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
        strokeWidth = 4f
    }
    drawIcon(canvas, iconPaint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

private fun createPersonMarkerBitmap(): BitmapDescriptor {
    return createMarkerBitmap(0xFFFF9800.toInt()) { canvas, paint ->
        // Head - circle
        canvas.drawCircle(48f, 30f, 12f, paint)
        // Body - rounded rect
        val bodyRect = android.graphics.RectF(28f, 44f, 68f, 76f)
        canvas.drawRoundRect(bodyRect, 20f, 20f, paint)
    }
}

private fun createHospitalMarkerBitmap(): BitmapDescriptor {
    return createMarkerBitmap(0xFFF44336.toInt()) { canvas, paint ->
        paint.strokeWidth = 8f
        paint.strokeCap = Paint.Cap.ROUND
        // Horizontal bar of cross
        canvas.drawLine(30f, 48f, 66f, 48f, paint)
        // Vertical bar of cross
        canvas.drawLine(48f, 30f, 48f, 66f, paint)
    }
}

private fun createSelfMarkerBitmap(): BitmapDescriptor {
    return createMarkerBitmap(0xFF2196F3.toInt()) { canvas, paint ->
        // Outer ring
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawCircle(48f, 48f, 16f, paint)
        // Inner dot
        paint.style = Paint.Style.FILL
        canvas.drawCircle(48f, 48f, 7f, paint)
    }
}

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
