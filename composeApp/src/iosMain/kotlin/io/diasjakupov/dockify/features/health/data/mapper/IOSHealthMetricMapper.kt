package io.diasjakupov.dockify.features.health.data.mapper

import io.diasjakupov.dockify.features.health.domain.model.HealthMetric
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType
import platform.HealthKit.HKCategoryTypeIdentifierSleepAnalysis
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

/**
 * Mapper object for converting between HealthKit types and domain models.
 */
object IOSHealthMetricMapper {

    /**
     * Maps HealthMetricType to HKQuantityTypeIdentifier string.
     */
    fun HealthMetricType.toHKQuantityTypeIdentifier(): String? {
        return when (this) {
            HealthMetricType.STEPS -> HKQuantityTypeIdentifierStepCount
            HealthMetricType.HEART_RATE -> HKQuantityTypeIdentifierHeartRate
            HealthMetricType.BLOOD_PRESSURE_SYSTOLIC -> HKQuantityTypeIdentifierBloodPressureSystolic
            HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> HKQuantityTypeIdentifierBloodPressureDiastolic
            HealthMetricType.BLOOD_OXYGEN -> HKQuantityTypeIdentifierOxygenSaturation
            HealthMetricType.SLEEP_DURATION -> null // Sleep uses HKCategoryType
            HealthMetricType.CALORIES_BURNED -> HKQuantityTypeIdentifierActiveEnergyBurned
            HealthMetricType.DISTANCE -> HKQuantityTypeIdentifierDistanceWalkingRunning
            HealthMetricType.WEIGHT -> HKQuantityTypeIdentifierBodyMass
            HealthMetricType.HEIGHT -> HKQuantityTypeIdentifierHeight
            HealthMetricType.BODY_TEMPERATURE -> HKQuantityTypeIdentifierBodyTemperature
            HealthMetricType.RESPIRATORY_RATE -> HKQuantityTypeIdentifierRespiratoryRate
        }
    }

    /**
     * Maps HealthMetricType to HKQuantityType.
     */
    fun HealthMetricType.toHKQuantityType(): HKQuantityType? {
        val identifier = toHKQuantityTypeIdentifier() ?: return null
        return HKQuantityType.quantityTypeForIdentifier(identifier)
    }

    /**
     * Maps HealthMetricType to HKObjectType for authorization.
     */
    fun HealthMetricType.toHKObjectType(): HKObjectType? {
        return when (this) {
            HealthMetricType.SLEEP_DURATION -> {
                HKObjectType.categoryTypeForIdentifier(HKCategoryTypeIdentifierSleepAnalysis)
            }
            else -> toHKQuantityType()
        }
    }

    /**
     * Creates a HealthMetric from quantity value.
     */
    fun createHealthMetric(type: HealthMetricType, value: Double, timestamp: Long? = null): HealthMetric {
        return HealthMetric(
            type = type,
            value = value,
            unit = type.defaultUnit,
            timestamp = timestamp
        )
    }

    /**
     * Creates a HealthMetric from Steps data.
     */
    fun createStepsMetric(count: Double, timestamp: Long? = null): HealthMetric {
        return HealthMetric(
            type = HealthMetricType.STEPS,
            value = count,
            unit = HealthMetricType.STEPS.defaultUnit,
            timestamp = timestamp
        )
    }

    /**
     * Creates a HealthMetric from HeartRate data.
     */
    fun createHeartRateMetric(bpm: Double, timestamp: Long? = null): HealthMetric {
        return HealthMetric(
            type = HealthMetricType.HEART_RATE,
            value = bpm,
            unit = HealthMetricType.HEART_RATE.defaultUnit,
            timestamp = timestamp
        )
    }

    /**
     * Creates a HealthMetric from BloodPressure systolic data.
     */
    fun createBloodPressureSystolicMetric(mmHg: Double, timestamp: Long? = null): HealthMetric {
        return HealthMetric(
            type = HealthMetricType.BLOOD_PRESSURE_SYSTOLIC,
            value = mmHg,
            unit = HealthMetricType.BLOOD_PRESSURE_SYSTOLIC.defaultUnit,
            timestamp = timestamp
        )
    }

    /**
     * Creates a HealthMetric from BloodPressure diastolic data.
     */
    fun createBloodPressureDiastolicMetric(mmHg: Double, timestamp: Long? = null): HealthMetric {
        return HealthMetric(
            type = HealthMetricType.BLOOD_PRESSURE_DIASTOLIC,
            value = mmHg,
            unit = HealthMetricType.BLOOD_PRESSURE_DIASTOLIC.defaultUnit,
            timestamp = timestamp
        )
    }

    /**
     * Creates a HealthMetric from OxygenSaturation data.
     */
    fun createBloodOxygenMetric(percentage: Double, timestamp: Long? = null): HealthMetric {
        return HealthMetric(
            type = HealthMetricType.BLOOD_OXYGEN,
            value = percentage * 100, // Convert from 0-1 to percentage
            unit = HealthMetricType.BLOOD_OXYGEN.defaultUnit,
            timestamp = timestamp
        )
    }

    /**
     * Creates a HealthMetric from Sleep duration in hours.
     */
    fun createSleepDurationMetric(hours: Double, timestamp: Long? = null): HealthMetric {
        return HealthMetric(
            type = HealthMetricType.SLEEP_DURATION,
            value = hours,
            unit = HealthMetricType.SLEEP_DURATION.defaultUnit,
            timestamp = timestamp
        )
    }

    /**
     * Creates a HealthMetric from ActiveCaloriesBurned data.
     */
    fun createCaloriesBurnedMetric(kcal: Double, timestamp: Long? = null): HealthMetric {
        return HealthMetric(
            type = HealthMetricType.CALORIES_BURNED,
            value = kcal,
            unit = HealthMetricType.CALORIES_BURNED.defaultUnit,
            timestamp = timestamp
        )
    }

    /**
     * Creates a HealthMetric from Distance data in kilometers.
     */
    fun createDistanceMetric(km: Double, timestamp: Long? = null): HealthMetric {
        return HealthMetric(
            type = HealthMetricType.DISTANCE,
            value = km,
            unit = HealthMetricType.DISTANCE.defaultUnit,
            timestamp = timestamp
        )
    }

    /**
     * Creates a HealthMetric from Weight data in kilograms.
     */
    fun createWeightMetric(kg: Double, timestamp: Long? = null): HealthMetric {
        return HealthMetric(
            type = HealthMetricType.WEIGHT,
            value = kg,
            unit = HealthMetricType.WEIGHT.defaultUnit,
            timestamp = timestamp
        )
    }

    /**
     * Creates a HealthMetric from Height data in centimeters.
     */
    fun createHeightMetric(cm: Double, timestamp: Long? = null): HealthMetric {
        return HealthMetric(
            type = HealthMetricType.HEIGHT,
            value = cm,
            unit = HealthMetricType.HEIGHT.defaultUnit,
            timestamp = timestamp
        )
    }

    /**
     * Creates a HealthMetric from BodyTemperature data in Celsius.
     */
    fun createBodyTemperatureMetric(celsius: Double, timestamp: Long? = null): HealthMetric {
        return HealthMetric(
            type = HealthMetricType.BODY_TEMPERATURE,
            value = celsius,
            unit = HealthMetricType.BODY_TEMPERATURE.defaultUnit,
            timestamp = timestamp
        )
    }

    /**
     * Creates a HealthMetric from RespiratoryRate data.
     */
    fun createRespiratoryRateMetric(breathsPerMin: Double, timestamp: Long? = null): HealthMetric {
        return HealthMetric(
            type = HealthMetricType.RESPIRATORY_RATE,
            value = breathsPerMin,
            unit = HealthMetricType.RESPIRATORY_RATE.defaultUnit,
            timestamp = timestamp
        )
    }
}
