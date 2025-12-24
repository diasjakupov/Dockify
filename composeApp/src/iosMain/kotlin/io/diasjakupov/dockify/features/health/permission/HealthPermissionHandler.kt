package io.diasjakupov.dockify.features.health.permission

import kotlinx.cinterop.ExperimentalForeignApi
import platform.HealthKit.HKAuthorizationStatusSharingAuthorized
import platform.HealthKit.HKCategoryTypeIdentifierSleepAnalysis
import platform.HealthKit.HKHealthStore
import platform.HealthKit.HKObjectType
import platform.HealthKit.HKQuantityType
import platform.HealthKit.HKQuantityTypeIdentifierActiveEnergyBurned
import platform.HealthKit.HKQuantityTypeIdentifierBloodPressureDiastolic
import platform.HealthKit.HKQuantityTypeIdentifierBloodPressureSystolic
import platform.HealthKit.HKQuantityTypeIdentifierBodyMass
import platform.HealthKit.HKQuantityTypeIdentifierBodyTemperature
import platform.HealthKit.HKQuantityTypeIdentifierDistanceWalkingRunning
import platform.HealthKit.HKQuantityTypeIdentifierHeartRate
import platform.HealthKit.HKQuantityTypeIdentifierHeight
import platform.HealthKit.HKQuantityTypeIdentifierOxygenSaturation
import platform.HealthKit.HKQuantityTypeIdentifierRespiratoryRate
import platform.HealthKit.HKQuantityTypeIdentifierStepCount
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of HealthPermissionHandler.
 *
 * Uses HKHealthStore to request and check HealthKit permissions.
 */
@OptIn(ExperimentalForeignApi::class)
actual class HealthPermissionHandler {

    private val healthStore = HKHealthStore()

    actual suspend fun requestHealthPermissions(): Boolean {
        if (!isHealthKitAvailable()) {
            return false
        }

        val readTypes = getAllRequiredHealthTypes()

        if (readTypes.isEmpty()) {
            return false
        }

        return suspendCoroutine { continuation ->
            healthStore.requestAuthorizationToShareTypes(
                typesToShare = null, // Read-only, no write permissions
                readTypes = readTypes
            ) { success, _ ->
                if (success) {
                    // Check if permissions were actually granted
                    val allGranted = readTypes.all { type ->
                        healthStore.authorizationStatusForType(type) == HKAuthorizationStatusSharingAuthorized
                    }
                    continuation.resume(allGranted)
                } else {
                    continuation.resume(false)
                }
            }
        }
    }

    actual fun hasHealthPermissions(): Boolean {
        if (!isHealthKitAvailable()) {
            return false
        }

        val requiredTypes = getAllRequiredHealthTypes()
        return requiredTypes.all { type ->
            healthStore.authorizationStatusForType(type) == HKAuthorizationStatusSharingAuthorized
        }
    }

    /**
     * Checks if HealthKit is available on this device.
     */
    fun isHealthKitAvailable(): Boolean {
        return HKHealthStore.isHealthDataAvailable()
    }

    /**
     * Returns all required HKObjectTypes for health data reading.
     */
    private fun getAllRequiredHealthTypes(): Set<HKObjectType> {
        val types = mutableSetOf<HKObjectType>()

        // Quantity types
        listOf(
            HKQuantityTypeIdentifierStepCount,
            HKQuantityTypeIdentifierHeartRate,
            HKQuantityTypeIdentifierBloodPressureSystolic,
            HKQuantityTypeIdentifierBloodPressureDiastolic,
            HKQuantityTypeIdentifierOxygenSaturation,
            HKQuantityTypeIdentifierActiveEnergyBurned,
            HKQuantityTypeIdentifierDistanceWalkingRunning,
            HKQuantityTypeIdentifierBodyMass,
            HKQuantityTypeIdentifierHeight,
            HKQuantityTypeIdentifierBodyTemperature,
            HKQuantityTypeIdentifierRespiratoryRate
        ).forEach { identifier ->
            HKQuantityType.quantityTypeForIdentifier(identifier)?.let { types.add(it) }
        }

        // Category types (Sleep)
        HKObjectType.categoryTypeForIdentifier(HKCategoryTypeIdentifierSleepAnalysis)?.let {
            types.add(it)
        }

        return types
    }
}
