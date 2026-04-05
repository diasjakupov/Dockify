package io.diasjakupov.dockify.features.location.presentation.nearby

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import io.diasjakupov.dockify.features.location.domain.model.Location
import io.diasjakupov.dockify.features.location.domain.model.NearbyUser
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation
import platform.UIKit.UIUserInterfaceStyle

@Composable
actual fun MapView(
    userLocation: Location?,
    nearbyUsers: List<NearbyUser>,
    darkTheme: Boolean,
    modifier: Modifier
) {
    val lastCentredLocation = remember { mutableStateOf<Location?>(null) }

    UIKitView(
        factory = { MKMapView().also { it.showsUserLocation = true } },
        modifier = modifier,
        update = { mapView ->
            // Adapt map appearance to theme
            mapView.overrideUserInterfaceStyle = if (darkTheme) {
                UIUserInterfaceStyle.UIUserInterfaceStyleDark
            } else {
                UIUserInterfaceStyle.UIUserInterfaceStyleLight
            }

            // Remove only custom annotations (not the built-in user location dot)
            val customAnnotations = mapView.annotations.filter { it !is platform.MapKit.MKUserLocation }
            mapView.removeAnnotations(customAnnotations)

            // Nearby user pins
            nearbyUsers.forEach { user ->
                val annotation = MKPointAnnotation()
                annotation.coordinate = CLLocationCoordinate2DMake(
                    user.location.latitude,
                    user.location.longitude
                )
                annotation.title = "User ${user.userId.take(6)}"
                mapView.addAnnotation(annotation)
            }

            // Centre on user location only when it first becomes available or changes
            userLocation?.let { loc ->
                if (lastCentredLocation.value != loc) {
                    lastCentredLocation.value = loc
                    val region = MKCoordinateRegionMakeWithDistance(
                        CLLocationCoordinate2DMake(loc.latitude, loc.longitude),
                        2000.0, 2000.0
                    )
                    mapView.setRegion(region, animated = true)
                }
            }
        }
    )
}
