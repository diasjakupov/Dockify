package io.diasjakupov.dockify.features.health.permission

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of HealthPermissionHandler.
 *
 * Uses Health Connect's permission system with a callback pattern where the UI layer
 * observes a trigger and launches the permission contract.
 */
actual class HealthPermissionHandler(
    private val context: Context
) {
    private val healthConnectClient: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    private var permissionDeferred: CompletableDeferred<Boolean>? = null

    private val _permissionRequestTrigger = MutableStateFlow(0L)

    /**
     * Flow that emits when a permission request should be shown.
     * The UI layer should observe this and launch the permission contract.
     */
    val permissionRequestTrigger: StateFlow<Long> = _permissionRequestTrigger.asStateFlow()

    actual suspend fun requestHealthPermissions(): Boolean {
        // First check if we already have permissions
        if (hasHealthPermissions()) {
            return true
        }

        // Check if Health Connect is available
        if (!isHealthConnectAvailable()) {
            return false
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

    actual fun hasHealthPermissions(): Boolean {
        if (!isHealthConnectAvailable()) {
            return false
        }

        return try {
            // Check synchronously using runBlocking is not ideal,
            // but for UI purposes we need a sync check
            // The UI will call the suspend version for actual checks
            false // Will be checked via suspend function in ViewModel
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Suspending version of permission check - use this for actual checks.
     */
    suspend fun hasHealthPermissionsSuspend(): Boolean {
        if (!isHealthConnectAvailable()) {
            return false
        }

        return try {
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            grantedPermissions.containsAll(REQUIRED_PERMISSIONS)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Called by the UI layer when the permission result is received.
     *
     * @param grantedPermissions The set of permissions that were granted
     */
    fun onPermissionResult(grantedPermissions: Set<String>) {
        val allGranted = grantedPermissions.containsAll(REQUIRED_PERMISSIONS)
        permissionDeferred?.complete(allGranted)
    }

    /**
     * Checks if Health Connect is available on this device.
     */
    fun isHealthConnectAvailable(): Boolean {
        val status = HealthConnectClient.getSdkStatus(context)
        return status == HealthConnectClient.SDK_AVAILABLE
    }

    companion object {
        /**
         * All required health permissions for reading health data.
         */
        val REQUIRED_PERMISSIONS = setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(BloodPressureRecord::class),
            HealthPermission.getReadPermission(OxygenSaturationRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class),
            HealthPermission.getReadPermission(WeightRecord::class),
            HealthPermission.getReadPermission(HeightRecord::class),
            HealthPermission.getReadPermission(BodyTemperatureRecord::class),
            HealthPermission.getReadPermission(RespiratoryRateRecord::class)
        )
    }
}
