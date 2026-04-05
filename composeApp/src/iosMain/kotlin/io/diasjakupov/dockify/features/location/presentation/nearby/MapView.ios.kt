package io.diasjakupov.dockify.features.location.presentation.nearby

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import io.diasjakupov.dockify.features.location.domain.model.Hospital
import io.diasjakupov.dockify.features.location.domain.model.Location
import io.diasjakupov.dockify.features.location.domain.model.NearbyUser
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKMarkerAnnotationView
import platform.MapKit.MKPointAnnotation
import platform.UIKit.UIColor
import platform.UIKit.UIUserInterfaceStyle
import platform.darwin.NSObject

private class HospitalAnnotation : MKPointAnnotation() {
    var hospital: Hospital? = null
}

@Composable
actual fun MapView(
    userLocation: Location?,
    nearbyUsers: List<NearbyUser>,
    nearbyHospitals: List<Hospital>,
    onHospitalClick: (Hospital) -> Unit,
    darkTheme: Boolean,
    modifier: Modifier
) {
    val lastCentredLocation = remember { mutableStateOf<Location?>(null) }

    val delegate = remember(onHospitalClick) {
        object : NSObject(), MKMapViewDelegateProtocol {
            override fun mapView(
                mapView: MKMapView,
                viewForAnnotation: MKAnnotationProtocol
            ): MKAnnotationView? {
                if (viewForAnnotation is platform.MapKit.MKUserLocation) return null

                val identifier = if (viewForAnnotation is HospitalAnnotation) "hospital" else "user"
                val annotationView = mapView.dequeueReusableAnnotationViewWithIdentifier(identifier)
                    as? MKMarkerAnnotationView
                    ?: MKMarkerAnnotationView(annotation = viewForAnnotation, reuseIdentifier = identifier)

                annotationView.annotation = viewForAnnotation

                if (viewForAnnotation is HospitalAnnotation) {
                    annotationView.markerTintColor = UIColor.redColor
                    annotationView.glyphText = "H"
                    annotationView.canShowCallout = true
                } else {
                    annotationView.markerTintColor = UIColor.orangeColor
                    annotationView.glyphText = "P"
                    annotationView.canShowCallout = true
                }

                return annotationView
            }

            override fun mapView(
                mapView: MKMapView,
                annotationView: MKAnnotationView,
                calloutAccessoryControlTapped: platform.UIKit.UIControl
            ) {
                val annotation = annotationView.annotation
                if (annotation is HospitalAnnotation) {
                    annotation.hospital?.let { onHospitalClick(it) }
                }
            }
        }
    }

    UIKitView(
        factory = {
            MKMapView().also {
                it.showsUserLocation = true
                it.delegate = delegate
            }
        },
        modifier = modifier,
        update = { mapView ->
            mapView.overrideUserInterfaceStyle = if (darkTheme) {
                UIUserInterfaceStyle.UIUserInterfaceStyleDark
            } else {
                UIUserInterfaceStyle.UIUserInterfaceStyleLight
            }

            val customAnnotations = mapView.annotations.filter { it !is platform.MapKit.MKUserLocation }
            mapView.removeAnnotations(customAnnotations)

            // User annotations
            nearbyUsers.forEach { user ->
                val annotation = MKPointAnnotation()
                annotation.coordinate = CLLocationCoordinate2DMake(
                    user.location.latitude,
                    user.location.longitude
                )
                annotation.title = "User ${user.userId.take(6)}"
                mapView.addAnnotation(annotation)
            }

            // Hospital annotations
            nearbyHospitals.forEachIndexed { index, hospital ->
                val annotation = HospitalAnnotation()
                annotation.hospital = hospital
                annotation.coordinate = CLLocationCoordinate2DMake(
                    hospital.location.latitude,
                    hospital.location.longitude
                )
                annotation.title = hospital.name ?: generateHospitalName(index)
                annotation.subtitle = "Tap for directions"
                mapView.addAnnotation(annotation)
            }

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
