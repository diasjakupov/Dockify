package io.diasjakupov.dockify.features.location.data.datasource

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.location.domain.model.Location
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation of LocationPlatformDataSource using FusedLocationProviderClient.
 */
class AndroidLocationPlatformDataSource(
    private val context: Context
) : LocationPlatformDataSource {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override suspend fun getCurrentLocation(): Resource<Location, DataError> {
        if (!hasPermission()) {
            return Resource.Error(DataError.Location.PERMISSION_DENIED)
        }

        if (!isLocationEnabled()) {
            return Resource.Error(DataError.Location.GPS_DISABLED)
        }

        return suspendCancellableCoroutine { continuation ->
            try {
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    1000L
                ).setMaxUpdates(1).build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        fusedLocationClient.removeLocationUpdates(this)
                        val androidLocation = result.lastLocation
                        if (androidLocation != null) {
                            val location = Location(
                                latitude = androidLocation.latitude,
                                longitude = androidLocation.longitude
                            )
                            if (continuation.isActive) {
                                continuation.resume(Resource.Success(location))
                            }
                        } else {
                            if (continuation.isActive) {
                                continuation.resume(Resource.Error(DataError.Location.LOCATION_UNAVAILABLE))
                            }
                        }
                    }
                }

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )

                continuation.invokeOnCancellation {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            } catch (e: SecurityException) {
                if (continuation.isActive) {
                    continuation.resume(Resource.Error(DataError.Location.PERMISSION_DENIED))
                }
            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resume(Resource.Error(DataError.Location.LOCATION_UNAVAILABLE))
                }
            }
        }
    }

    override fun observeLocation(): Flow<Resource<Location, DataError>> = callbackFlow {
        if (!hasPermission()) {
            trySend(Resource.Error(DataError.Location.PERMISSION_DENIED))
            close()
            return@callbackFlow
        }

        if (!isLocationEnabled()) {
            trySend(Resource.Error(DataError.Location.GPS_DISABLED))
            close()
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // Update every 10 seconds
        ).setMinUpdateIntervalMillis(5000L).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val androidLocation = result.lastLocation
                if (androidLocation != null) {
                    val location = Location(
                        latitude = androidLocation.latitude,
                        longitude = androidLocation.longitude
                    )
                    trySend(Resource.Success(location))
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            trySend(Resource.Error(DataError.Location.PERMISSION_DENIED))
            close()
            return@callbackFlow
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override suspend fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestPermission(): EmptyResult<DataError> {
        // This method signals that permissions need to be requested.
        // The actual permission request is handled by the UI layer using
        // ActivityResultLauncher or rememberLauncherForActivityResult.
        return Resource.Success(Unit)
    }

    override suspend fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
