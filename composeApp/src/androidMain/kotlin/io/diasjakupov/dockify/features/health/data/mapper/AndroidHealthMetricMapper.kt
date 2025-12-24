package io.diasjakupov.dockify.features.health.data.mapper

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import io.diasjakupov.dockify.features.health.domain.model.HealthMetric
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType
import kotlin.reflect.KClass

/**
 * Mapper object for converting between Health Connect types and domain models.
 */
object AndroidHealthMetricMapper {

    /**
     * Maps HealthMetricType to Health Connect Record class.
     */
    fun HealthMetricType.toRecordClass(): KClass<out Record>? {
        return when (this) {
            HealthMetricType.STEPS -> StepsRecord::class
            HealthMetricType.HEART_RATE -> HeartRateRecord::class
            HealthMetricType.BLOOD_PRESSURE_SYSTOLIC,
            HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> BloodPressureRecord::class
            HealthMetricType.BLOOD_OXYGEN -> OxygenSaturationRecord::class
            HealthMetricType.SLEEP_DURATION -> SleepSessionRecord::class
            HealthMetricType.CALORIES_BURNED -> ActiveCaloriesBurnedRecord::class
            HealthMetricType.DISTANCE -> DistanceRecord::class
            HealthMetricType.WEIGHT -> WeightRecord::class
            HealthMetricType.HEIGHT -> HeightRecord::class
            HealthMetricType.BODY_TEMPERATURE -> BodyTemperatureRecord::class
            HealthMetricType.RESPIRATORY_RATE -> RespiratoryRateRecord::class
        }
    }

    /**
     * Maps HealthMetricType to Health Connect read permission string.
     */
    fun HealthMetricType.toReadPermission(): String {
        return when (this) {
            HealthMetricType.STEPS -> HealthPermission.getReadPermission(StepsRecord::class)
            HealthMetricType.HEART_RATE -> HealthPermission.getReadPermission(HeartRateRecord::class)
            HealthMetricType.BLOOD_PRESSURE_SYSTOLIC,
            HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> HealthPermission.getReadPermission(BloodPressureRecord::class)
            HealthMetricType.BLOOD_OXYGEN -> HealthPermission.getReadPermission(OxygenSaturationRecord::class)
            HealthMetricType.SLEEP_DURATION -> HealthPermission.getReadPermission(SleepSessionRecord::class)
            HealthMetricType.CALORIES_BURNED -> HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)
            HealthMetricType.DISTANCE -> HealthPermission.getReadPermission(DistanceRecord::class)
            HealthMetricType.WEIGHT -> HealthPermission.getReadPermission(WeightRecord::class)
            HealthMetricType.HEIGHT -> HealthPermission.getReadPermission(HeightRecord::class)
            HealthMetricType.BODY_TEMPERATURE -> HealthPermission.getReadPermission(BodyTemperatureRecord::class)
            HealthMetricType.RESPIRATORY_RATE -> HealthPermission.getReadPermission(RespiratoryRateRecord::class)
        }
    }

    /**
     * Converts a list of HealthMetricTypes to Health Connect permission set.
     */
    fun List<HealthMetricType>.toHealthConnectPermissions(): Set<String> {
        return this.map { it.toReadPermission() }.toSet()
    }

    /**
     * Creates a HealthMetric from Steps data.
     */
    fun createStepsMetric(count: Long, timestamp: Long? = null): HealthMetric {
        return HealthMetric(
            type = HealthMetricType.STEPS,
            value = count.toDouble(),
            unit = HealthMetricType.STEPS.defaultUnit,
            timestamp = timestamp
        )
    }

    /**
     * Creates a HealthMetric from HeartRate data.
     */
    fun createHeartRateMetric(bpm: Long, timestamp: Long? = null): HealthMetric {
        return HealthMetric(
            type = HealthMetricType.HEART_RATE,
            value = bpm.toDouble(),
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
            value = percentage,
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
