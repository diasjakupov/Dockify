package io.diasjakupov.dockify.features.location.presentation.nearby

import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKLaunchOptionsDirectionsModeDefault
import platform.MapKit.MKLaunchOptionsDirectionsModeKey
import platform.MapKit.MKMapItem
import platform.MapKit.MKPlacemark

actual fun openDirectionsToLocation(latitude: Double, longitude: Double, label: String) {
    val coordinate = CLLocationCoordinate2DMake(latitude, longitude)
    val placemark = MKPlacemark(coordinate = coordinate)
    val mapItem = MKMapItem(placemark = placemark)
    mapItem.name = label
    mapItem.openInMapsWithLaunchOptions(
        mapOf(MKLaunchOptionsDirectionsModeKey to MKLaunchOptionsDirectionsModeDefault)
    )
}
