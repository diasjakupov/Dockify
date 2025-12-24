package io.diasjakupov.dockify.features.health.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.health.data.mapper.IOSHealthMetricMapper
import io.diasjakupov.dockify.features.health.data.mapper.IOSHealthMetricMapper.toHKObjectType
import io.diasjakupov.dockify.features.health.data.mapper.IOSHealthMetricMapper.toHKQuantityType
import io.diasjakupov.dockify.features.health.domain.model.HealthMetric
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSPredicate
import platform.Foundation.NSSortDescriptor
import platform.Foundation.timeIntervalSince1970
import platform.HealthKit.HKAuthorizationStatusSharingAuthorized
import platform.HealthKit.HKCategorySample
import platform.HealthKit.HKCategoryTypeIdentifierSleepAnalysis
import platform.HealthKit.HKHealthStore
import platform.HealthKit.HKObjectType
import platform.HealthKit.HKQuantitySample
import platform.HealthKit.HKQuantityType
import platform.HealthKit.HKQuery
import platform.HealthKit.HKQueryOptionNone
import platform.HealthKit.HKSampleQuery
import platform.HealthKit.HKSampleSortIdentifierEndDate
import platform.HealthKit.HKStatisticsOptionCumulativeSum
import platform.HealthKit.HKStatisticsQuery
import platform.HealthKit.HKUnit
import platform.HealthKit.predicateForSamplesWithStartDate
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of HealthPlatformDataSource using HealthKit.
 */
@OptIn(ExperimentalForeignApi::class)
class IOSHealthPlatformDataSource : HealthPlatformDataSource {

    private val healthStore = HKHealthStore()

    // HKUnit instances created using unitFromString
    private val countUnit = HKUnit.unitFromString("count")
    private val minuteUnit = HKUnit.unitFromString("min")
    private val kilocalorieUnit = HKUnit.unitFromString("kcal")
    private val meterUnit = HKUnit.unitFromString("m")
    private val gramUnit = HKUnit.unitFromString("g")
    private val percentUnit = HKUnit.unitFromString("%")
    private val mmHgUnit = HKUnit.unitFromString("mmHg")
    private val celsiusUnit = HKUnit.unitFromString("degC")
    private val beatsPerMinUnit = HKUnit.unitFromString("count/min")
    private val breathsPerMinUnit = HKUnit.unitFromString("count/min")

    override suspend fun isAvailable(): Boolean {
        return HKHealthStore.isHealthDataAvailable()
    }

    override suspend fun hasPermissions(types: List<HealthMetricType>): Boolean {
        if (!isAvailable()) return false

        return types.all { type ->
            val hkType = type.toHKObjectType() ?: return@all false
            val status = healthStore.authorizationStatusForType(hkType)
            status == HKAuthorizationStatusSharingAuthorized
        }
    }

    override suspend fun requestPermissions(types: List<HealthMetricType>): EmptyResult<DataError> {
        if (!isAvailable()) {
            return Resource.Error(DataError.Health.HEALTHKIT_NOT_AVAILABLE)
        }

        val readTypes = types.mapNotNull { it.toHKObjectType() }.toSet()

        if (readTypes.isEmpty()) {
            return Resource.Success(Unit)
        }

        return suspendCoroutine { continuation ->
            healthStore.requestAuthorizationToShareTypes(
                typesToShare = null, // Read-only, no write permissions
                readTypes = readTypes
            ) { success, _ ->
                if (success) {
                    continuation.resume(Resource.Success(Unit))
                } else {
                    continuation.resume(Resource.Error(DataError.Health.PERMISSION_DENIED))
                }
            }
        }
    }

    override suspend fun readHealthData(types: List<HealthMetricType>): Resource<List<HealthMetric>, DataError> {
        if (!isAvailable()) {
            return Resource.Error(DataError.Health.HEALTHKIT_NOT_AVAILABLE)
        }

        return try {
            val metrics = mutableListOf<HealthMetric>()
            val calendar = NSCalendar.currentCalendar
            val now = NSDate()
            val startOfDay = calendar.startOfDayForDate(now)

            types.forEach { type ->
                when (type) {
                    HealthMetricType.STEPS -> readCumulativeQuantity(type, startOfDay, now)?.let { metrics.add(it) }
                    HealthMetricType.HEART_RATE -> readLatestQuantitySample(type, startOfDay, now)?.let { metrics.add(it) }
                    HealthMetricType.BLOOD_PRESSURE_SYSTOLIC,
                    HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> readLatestQuantitySample(type, startOfDay, now)?.let { metrics.add(it) }
                    HealthMetricType.BLOOD_OXYGEN -> readLatestQuantitySample(type, startOfDay, now)?.let { metrics.add(it) }
                    HealthMetricType.SLEEP_DURATION -> readSleepDuration(startOfDay, now)?.let { metrics.add(it) }
                    HealthMetricType.CALORIES_BURNED -> readCumulativeQuantity(type, startOfDay, now)?.let { metrics.add(it) }
                    HealthMetricType.DISTANCE -> readCumulativeQuantity(type, startOfDay, now)?.let { metrics.add(it) }
                    HealthMetricType.WEIGHT -> readLatestQuantitySample(type, startOfDay, now)?.let { metrics.add(it) }
                    HealthMetricType.HEIGHT -> readLatestQuantitySample(type, startOfDay, now)?.let { metrics.add(it) }
                    HealthMetricType.BODY_TEMPERATURE -> readLatestQuantitySample(type, startOfDay, now)?.let { metrics.add(it) }
                    HealthMetricType.RESPIRATORY_RATE -> readLatestQuantitySample(type, startOfDay, now)?.let { metrics.add(it) }
                }
            }

            Resource.Success(metrics)
        } catch (e: Exception) {
            Resource.Error(DataError.Health.DATA_NOT_FOUND)
        }
    }

    /**
     * Creates a predicate for filtering samples by date range.
     */
    private fun createDatePredicate(startDate: NSDate, endDate: NSDate): NSPredicate? {
        return HKQuery.predicateForSamplesWithStartDate(
            startDate = startDate,
            endDate = endDate,
            options = HKQueryOptionNone
        )
    }

    /**
     * Reads cumulative quantity data (steps, calories, distance) using HKStatisticsQuery.
     */
    private suspend fun readCumulativeQuantity(
        type: HealthMetricType,
        startDate: NSDate,
        endDate: NSDate
    ): HealthMetric? {
        val quantityType = type.toHKQuantityType() ?: return null
        val predicate = createDatePredicate(startDate, endDate)

        return suspendCoroutine { continuation ->
            val query = HKStatisticsQuery(
                quantityType = quantityType,
                quantitySamplePredicate = predicate,
                options = HKStatisticsOptionCumulativeSum
            ) { _, statistics, error ->
                if (error != null || statistics == null) {
                    continuation.resume(null)
                    return@HKStatisticsQuery
                }

                val sum = statistics.sumQuantity()
                if (sum == null) {
                    continuation.resume(null)
                    return@HKStatisticsQuery
                }

                val value = when (type) {
                    HealthMetricType.STEPS -> sum.doubleValueForUnit(countUnit)
                    HealthMetricType.CALORIES_BURNED -> sum.doubleValueForUnit(kilocalorieUnit)
                    HealthMetricType.DISTANCE -> sum.doubleValueForUnit(meterUnit) / 1000.0 // Convert to km
                    else -> sum.doubleValueForUnit(countUnit)
                }

                val timestamp = (endDate.timeIntervalSince1970 * 1000).toLong()
                val metric = IOSHealthMetricMapper.createHealthMetric(type, value, timestamp)
                continuation.resume(metric)
            }
            healthStore.executeQuery(query)
        }
    }

    /**
     * Reads the latest quantity sample (heart rate, blood pressure, weight, etc.).
     */
    private suspend fun readLatestQuantitySample(
        type: HealthMetricType,
        startDate: NSDate,
        endDate: NSDate
    ): HealthMetric? {
        val quantityType = type.toHKQuantityType() ?: return null
        val predicate = createDatePredicate(startDate, endDate)
        val sortDescriptor = NSSortDescriptor(key = HKSampleSortIdentifierEndDate, ascending = false)

        return suspendCoroutine { continuation ->
            val query = HKSampleQuery(
                sampleType = quantityType,
                predicate = predicate,
                limit = 1u,
                sortDescriptors = listOf(sortDescriptor)
            ) { _, results, error ->
                if (error != null || results == null || results.isEmpty()) {
                    continuation.resume(null)
                    return@HKSampleQuery
                }

                val sample = results.firstOrNull() as? HKQuantitySample
                if (sample == null) {
                    continuation.resume(null)
                    return@HKSampleQuery
                }

                val value = extractQuantityValue(sample, type)
                val timestamp = (sample.endDate.timeIntervalSince1970 * 1000).toLong()

                val metric = when (type) {
                    HealthMetricType.STEPS -> IOSHealthMetricMapper.createStepsMetric(value, timestamp)
                    HealthMetricType.HEART_RATE -> IOSHealthMetricMapper.createHeartRateMetric(value, timestamp)
                    HealthMetricType.BLOOD_PRESSURE_SYSTOLIC -> IOSHealthMetricMapper.createBloodPressureSystolicMetric(value, timestamp)
                    HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> IOSHealthMetricMapper.createBloodPressureDiastolicMetric(value, timestamp)
                    HealthMetricType.BLOOD_OXYGEN -> IOSHealthMetricMapper.createBloodOxygenMetric(value, timestamp)
                    HealthMetricType.CALORIES_BURNED -> IOSHealthMetricMapper.createCaloriesBurnedMetric(value, timestamp)
                    HealthMetricType.DISTANCE -> IOSHealthMetricMapper.createDistanceMetric(value, timestamp)
                    HealthMetricType.WEIGHT -> IOSHealthMetricMapper.createWeightMetric(value, timestamp)
                    HealthMetricType.HEIGHT -> IOSHealthMetricMapper.createHeightMetric(value, timestamp)
                    HealthMetricType.BODY_TEMPERATURE -> IOSHealthMetricMapper.createBodyTemperatureMetric(value, timestamp)
                    HealthMetricType.RESPIRATORY_RATE -> IOSHealthMetricMapper.createRespiratoryRateMetric(value, timestamp)
                    else -> IOSHealthMetricMapper.createHealthMetric(type, value, timestamp)
                }
                continuation.resume(metric)
            }
            healthStore.executeQuery(query)
        }
    }

    /**
     * Extracts the numeric value from an HKQuantitySample with the appropriate unit.
     */
    private fun extractQuantityValue(sample: HKQuantitySample, type: HealthMetricType): Double {
        val quantity = sample.quantity
        return when (type) {
            HealthMetricType.STEPS -> quantity.doubleValueForUnit(countUnit)
            HealthMetricType.HEART_RATE -> quantity.doubleValueForUnit(beatsPerMinUnit)
            HealthMetricType.BLOOD_PRESSURE_SYSTOLIC,
            HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> quantity.doubleValueForUnit(mmHgUnit)
            HealthMetricType.BLOOD_OXYGEN -> quantity.doubleValueForUnit(percentUnit)
            HealthMetricType.CALORIES_BURNED -> quantity.doubleValueForUnit(kilocalorieUnit)
            HealthMetricType.DISTANCE -> quantity.doubleValueForUnit(meterUnit) / 1000.0 // Convert to km
            HealthMetricType.WEIGHT -> quantity.doubleValueForUnit(gramUnit) / 1000.0 // Convert to kg
            HealthMetricType.HEIGHT -> quantity.doubleValueForUnit(meterUnit) * 100.0 // Convert to cm
            HealthMetricType.BODY_TEMPERATURE -> quantity.doubleValueForUnit(celsiusUnit)
            HealthMetricType.RESPIRATORY_RATE -> quantity.doubleValueForUnit(breathsPerMinUnit)
            else -> quantity.doubleValueForUnit(countUnit)
        }
    }

    /**
     * Reads total sleep duration from sleep analysis category samples.
     */
    private suspend fun readSleepDuration(startDate: NSDate, endDate: NSDate): HealthMetric? {
        val sleepType = HKObjectType.categoryTypeForIdentifier(HKCategoryTypeIdentifierSleepAnalysis) ?: return null
        val predicate = createDatePredicate(startDate, endDate)

        return suspendCoroutine { continuation ->
            val query = HKSampleQuery(
                sampleType = sleepType,
                predicate = predicate,
                limit = 0u, // No limit, get all samples
                sortDescriptors = null
            ) { _, results, error ->
                if (error != null || results == null || results.isEmpty()) {
                    continuation.resume(null)
                    return@HKSampleQuery
                }

                // Calculate total sleep duration from all sleep samples
                var totalSeconds = 0.0
                results.forEach { sample ->
                    val categorySample = sample as? HKCategorySample
                    if (categorySample != null) {
                        val duration = categorySample.endDate.timeIntervalSince1970 - categorySample.startDate.timeIntervalSince1970
                        totalSeconds += duration
                    }
                }

                val hours = totalSeconds / 3600.0
                val timestamp = (endDate.timeIntervalSince1970 * 1000).toLong()
                val metric = IOSHealthMetricMapper.createSleepDurationMetric(hours, timestamp)
                continuation.resume(metric)
            }
            healthStore.executeQuery(query)
        }
    }
}
