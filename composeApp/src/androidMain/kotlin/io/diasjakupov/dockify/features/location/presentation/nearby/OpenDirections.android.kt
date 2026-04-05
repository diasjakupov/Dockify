package io.diasjakupov.dockify.features.location.presentation.nearby

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private object DirectionsLauncher : KoinComponent {
    val context: Context by inject()
}

actual fun openDirectionsToLocation(latitude: Double, longitude: Double, label: String) {
    val context = DirectionsLauncher.context
    val gmmUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmUri).apply {
        setPackage("com.google.android.apps.maps")
    }

    if (mapIntent.resolveActivity(context.packageManager) != null) {
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(mapIntent)
    } else {
        // Fallback: open any maps app via geo URI
        val fallbackUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
        val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(fallbackIntent)
    }
}
