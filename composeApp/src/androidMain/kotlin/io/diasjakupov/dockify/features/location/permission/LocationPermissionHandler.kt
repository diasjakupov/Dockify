package io.diasjakupov.dockify.features.location.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of LocationPermissionHandler.
 *
 * Uses a callback pattern where the UI layer (Activity/Composable) registers
 * to handle permission requests and notifies this handler of the result.
 */
actual class LocationPermissionHandler(
    private val context: Context
) {
    private var permissionDeferred: CompletableDeferred<Boolean>? = null

    private val _permissionRequestTrigger = MutableStateFlow(0L)

    /**
     * Flow that emits when a permission request should be shown.
     * The UI layer should observe this and show the permission dialog.
     */
    val permissionRequestTrigger: StateFlow<Long> = _permissionRequestTrigger.asStateFlow()

    actual suspend fun requestLocationPermission(): Boolean {
        // First check if we already have permission
        if (hasLocationPermission()) {
            return true
        }

        // Create a new deferred to wait for the result
        permissionDeferred = CompletableDeferred()

        // Trigger the UI to show permission dialog
        _permissionRequestTrigger.value = System.currentTimeMillis()

        // Wait for the result from the UI layer
        return try {
            permissionDeferred?.await() ?: false
        } finally {
            permissionDeferred = null
        }
    }

    actual fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Called by the UI layer when the permission result is received.
     *
     * @param granted true if permission was granted, false otherwise
     */
    fun onPermissionResult(granted: Boolean) {
        permissionDeferred?.complete(granted)
    }

    companion object {
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}
