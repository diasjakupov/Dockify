package io.diasjakupov.dockify.features.health.data.datasource

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.health.data.mapper.AndroidHealthMetricMapper
import io.diasjakupov.dockify.features.health.data.mapper.AndroidHealthMetricMapper.toHealthConnectPermissions
import io.diasjakupov.dockify.features.health.domain.model.HealthMetric
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType
import java.io.IOException
import java.time.Duration
import java.time.Instant

/**
 * Android implementation of HealthPlatformDataSource using Health Connect API.
 */
class AndroidHealthPlatformDataSource(
    private val context: Context
) : HealthPlatformDataSource {

    private val healthConnectClient: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    override suspend fun isAvailable(): Boolean {
        val status = HealthConnectClient.getSdkStatus(context)
        return status == HealthConnectClient.SDK_AVAILABLE
    }

    override suspend fun hasPermissions(types: List<HealthMetricType>): Boolean {
        if (!isAvailable()) return false

        val requiredPermissions = types.toHealthConnectPermissions()
        val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
        return grantedPermissions.containsAll(requiredPermissions)
    }

    override suspend fun requestPermissions(types: List<HealthMetricType>): EmptyResult<DataError> {
        // This method signals that permissions need to be requested.
        // The actual permission request is handled by the UI layer using
        // PermissionController.createRequestPermissionResultContract()
        // This is the recommended pattern for Health Connect.
        return Resource.Success(Unit)
    }

    override suspend fun readHealthData(types: List<HealthMetricType>): Resource<List<HealthMetric>, DataError> {
        if (!isAvailable()) {
            return Resource.Error(DataError.Health.HEALTH_CONNECT_NOT_AVAILABLE)
        }

        if (!hasPermissions(types)) {
            return Resource.Error(DataError.Health.PERMISSION_DENIED)
        }

        return try {
            val endTime = Instant.now()
            val startTime = endTime.minus(Duration.ofHours(24))
            val metrics = mutableListOf<HealthMetric>()

            types.forEach { type ->
                when (type) {
                    HealthMetricType.STEPS -> readAggregatedSteps(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.HEART_RATE -> readLatestHeartRate(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.BLOOD_PRESSURE_SYSTOLIC,
                    HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> {
                        readLatestBloodPressure(startTime, endTime, type)?.let { metrics.add(it) }
                    }
                    HealthMetricType.BLOOD_OXYGEN -> readLatestOxygenSaturation(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.SLEEP_DURATION -> readSleepDuration(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.CALORIES_BURNED -> readAggregatedCalories(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.DISTANCE -> readAggregatedDistance(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.WEIGHT -> readLatestWeight(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.HEIGHT -> readLatestHeight(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.BODY_TEMPERATURE -> readLatestBodyTemperature(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.RESPIRATORY_RATE -> readLatestRespiratoryRate(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.FAT_PERCENTAGE -> readLatestBodyFat(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.RESTING_BPM -> readLatestRestingHeartRate(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.MAX_BPM -> readMaxHeartRate(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.AVG_BPM -> readAvgHeartRate(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.SESSION_DURATION_HOURS -> readExerciseSessionDuration(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.WORKOUT_FREQUENCY -> readWorkoutFrequency(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.DAILY_CALORIES -> readDailyCalorieIntake(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.WATER_INTAKE_LITERS -> readWaterIntake(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.SLEEP_EFFICIENCY -> readSleepEfficiency(startTime, endTime)?.let { metrics.add(it) }
                    HealthMetricType.TIME_IN_BED_HOURS -> readTimeInBed(startTime, endTime)?.let { metrics.add(it) }
                    else -> { /* No Health Connect equivalent — backend-derived or lifestyle flags */ }
                }
            }

            Resource.Success(metrics)
        } catch (e: SecurityException) {
            Resource.Error(DataError.Health.PERMISSION_DENIED)
        } catch (e: IOException) {
            Resource.Error(DataError.Network.UNKNOWN)
        } catch (e: IllegalStateException) {
            Resource.Error(DataError.Health.HEALTH_CONNECT_NOT_AVAILABLE)
        } catch (e: Exception) {
            Resource.Error(DataError.Health.DATA_NOT_FOUND)
        }
    }

    /**
     * Reads aggregated steps count for the time range.
     * Uses aggregate() to avoid double counting from multiple sources.
     */
    private suspend fun readAggregatedSteps(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        val stepCount = response[StepsRecord.COUNT_TOTAL] ?: return null
        return AndroidHealthMetricMapper.createStepsMetric(stepCount, endTime.toEpochMilli())
    }

    /**
     * Reads the latest heart rate measurement.
     */
    private suspend fun readLatestHeartRate(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        val record = response.records.lastOrNull() ?: return null
        val sample = record.samples.lastOrNull() ?: return null
        return AndroidHealthMetricMapper.createHeartRateMetric(
            sample.beatsPerMinute,
            sample.time.toEpochMilli()
        )
    }

    /**
     * Reads the latest blood pressure measurement.
     */
    private suspend fun readLatestBloodPressure(
        startTime: Instant,
        endTime: Instant,
        type: HealthMetricType
    ): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = BloodPressureRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        val record = response.records.lastOrNull() ?: return null
        val timestamp = record.time.toEpochMilli()

        return when (type) {
            HealthMetricType.BLOOD_PRESSURE_SYSTOLIC ->
                AndroidHealthMetricMapper.createBloodPressureSystolicMetric(
                    record.systolic.inMillimetersOfMercury,
                    timestamp
                )
            HealthMetricType.BLOOD_PRESSURE_DIASTOLIC ->
                AndroidHealthMetricMapper.createBloodPressureDiastolicMetric(
                    record.diastolic.inMillimetersOfMercury,
                    timestamp
                )
            else -> null
        }
    }

    /**
     * Reads the latest oxygen saturation measurement.
     */
    private suspend fun readLatestOxygenSaturation(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = OxygenSaturationRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        val record = response.records.lastOrNull() ?: return null
        return AndroidHealthMetricMapper.createBloodOxygenMetric(
            record.percentage.value,
            record.time.toEpochMilli()
        )
    }

    /**
     * Reads total sleep duration from sleep sessions.
     */
    private suspend fun readSleepDuration(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        if (response.records.isEmpty()) return null

        // Calculate total sleep duration in hours
        val totalDurationMillis = response.records.sumOf { session ->
            Duration.between(session.startTime, session.endTime).toMillis()
        }
        val hours = totalDurationMillis / (1000.0 * 60 * 60)

        return AndroidHealthMetricMapper.createSleepDurationMetric(hours, endTime.toEpochMilli())
    }

    /**
     * Reads aggregated calories burned for the time range.
     */
    private suspend fun readAggregatedCalories(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.aggregate(
            AggregateRequest(
                metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        val calories = response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL] ?: return null
        return AndroidHealthMetricMapper.createCaloriesBurnedMetric(
            calories.inKilocalories,
            endTime.toEpochMilli()
        )
    }

    /**
     * Reads aggregated distance for the time range.
     */
    private suspend fun readAggregatedDistance(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.aggregate(
            AggregateRequest(
                metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        val distance = response[DistanceRecord.DISTANCE_TOTAL] ?: return null
        return AndroidHealthMetricMapper.createDistanceMetric(
            distance.inKilometers,
            endTime.toEpochMilli()
        )
    }

    /**
     * Reads the latest weight measurement.
     */
    private suspend fun readLatestWeight(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = WeightRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        val record = response.records.lastOrNull() ?: return null
        return AndroidHealthMetricMapper.createWeightMetric(
            record.weight.inKilograms,
            record.time.toEpochMilli()
        )
    }

    /**
     * Reads the latest height measurement.
     */
    private suspend fun readLatestHeight(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = HeightRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        val record = response.records.lastOrNull() ?: return null
        // Convert meters to centimeters
        return AndroidHealthMetricMapper.createHeightMetric(
            record.height.inMeters * 100,
            record.time.toEpochMilli()
        )
    }

    /**
     * Reads the latest body temperature measurement.
     */
    private suspend fun readLatestBodyTemperature(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = BodyTemperatureRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        val record = response.records.lastOrNull() ?: return null
        return AndroidHealthMetricMapper.createBodyTemperatureMetric(
            record.temperature.inCelsius,
            record.time.toEpochMilli()
        )
    }

    /**
     * Reads the latest respiratory rate measurement.
     */
    private suspend fun readLatestRespiratoryRate(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = RespiratoryRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        val record = response.records.lastOrNull() ?: return null
        return AndroidHealthMetricMapper.createRespiratoryRateMetric(
            record.rate,
            record.time.toEpochMilli()
        )
    }

    private suspend fun readLatestBodyFat(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(recordType = BodyFatRecord::class, timeRangeFilter = TimeRangeFilter.between(startTime, endTime))
        )
        val record = response.records.lastOrNull() ?: return null
        return HealthMetric(type = HealthMetricType.FAT_PERCENTAGE, value = record.percentage.value, unit = HealthMetricType.FAT_PERCENTAGE.defaultUnit, timestamp = record.time.toEpochMilli())
    }

    private suspend fun readLatestRestingHeartRate(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(recordType = RestingHeartRateRecord::class, timeRangeFilter = TimeRangeFilter.between(startTime, endTime))
        )
        val record = response.records.lastOrNull() ?: return null
        return HealthMetric(type = HealthMetricType.RESTING_BPM, value = record.beatsPerMinute.toDouble(), unit = HealthMetricType.RESTING_BPM.defaultUnit, timestamp = record.time.toEpochMilli())
    }

    private suspend fun readMaxHeartRate(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(recordType = HeartRateRecord::class, timeRangeFilter = TimeRangeFilter.between(startTime, endTime))
        )
        val maxBpm = response.records.flatMap { it.samples }.maxOfOrNull { it.beatsPerMinute } ?: return null
        return HealthMetric(type = HealthMetricType.MAX_BPM, value = maxBpm.toDouble(), unit = HealthMetricType.MAX_BPM.defaultUnit, timestamp = endTime.toEpochMilli())
    }

    private suspend fun readAvgHeartRate(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(recordType = HeartRateRecord::class, timeRangeFilter = TimeRangeFilter.between(startTime, endTime))
        )
        val samples = response.records.flatMap { it.samples }
        if (samples.isEmpty()) return null
        val avgBpm = samples.map { it.beatsPerMinute }.average()
        return HealthMetric(type = HealthMetricType.AVG_BPM, value = avgBpm, unit = HealthMetricType.AVG_BPM.defaultUnit, timestamp = endTime.toEpochMilli())
    }

    private suspend fun readExerciseSessionDuration(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(recordType = ExerciseSessionRecord::class, timeRangeFilter = TimeRangeFilter.between(startTime, endTime))
        )
        if (response.records.isEmpty()) return null
        val totalHours = response.records.sumOf { Duration.between(it.startTime, it.endTime).toMillis() } / (1000.0 * 60 * 60)
        return HealthMetric(type = HealthMetricType.SESSION_DURATION_HOURS, value = totalHours, unit = HealthMetricType.SESSION_DURATION_HOURS.defaultUnit, timestamp = endTime.toEpochMilli())
    }

    private suspend fun readWorkoutFrequency(startTime: Instant, endTime: Instant): HealthMetric? {
        val weekStart = endTime.minus(Duration.ofDays(7))
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(recordType = ExerciseSessionRecord::class, timeRangeFilter = TimeRangeFilter.between(weekStart, endTime))
        )
        if (response.records.isEmpty()) return null
        return HealthMetric(type = HealthMetricType.WORKOUT_FREQUENCY, value = response.records.size.toDouble(), unit = HealthMetricType.WORKOUT_FREQUENCY.defaultUnit, timestamp = endTime.toEpochMilli())
    }

    private suspend fun readDailyCalorieIntake(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(recordType = NutritionRecord::class, timeRangeFilter = TimeRangeFilter.between(startTime, endTime))
        )
        if (response.records.isEmpty()) return null
        val totalKcal = response.records.sumOf { it.energy?.inKilocalories ?: 0.0 }
        if (totalKcal == 0.0) return null
        return HealthMetric(type = HealthMetricType.DAILY_CALORIES, value = totalKcal, unit = HealthMetricType.DAILY_CALORIES.defaultUnit, timestamp = endTime.toEpochMilli())
    }

    private suspend fun readWaterIntake(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(recordType = HydrationRecord::class, timeRangeFilter = TimeRangeFilter.between(startTime, endTime))
        )
        if (response.records.isEmpty()) return null
        val totalLiters = response.records.sumOf { it.volume.inLiters }
        return HealthMetric(type = HealthMetricType.WATER_INTAKE_LITERS, value = totalLiters, unit = HealthMetricType.WATER_INTAKE_LITERS.defaultUnit, timestamp = endTime.toEpochMilli())
    }

    private suspend fun readSleepEfficiency(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(recordType = SleepSessionRecord::class, timeRangeFilter = TimeRangeFilter.between(startTime, endTime))
        )
        val session = response.records.lastOrNull() ?: return null
        val timeInBedMillis = Duration.between(session.startTime, session.endTime).toMillis()
        if (timeInBedMillis == 0L) return null
        val asleepMillis = session.stages.filter { it.stage == SleepSessionRecord.STAGE_TYPE_SLEEPING }.sumOf { Duration.between(it.startTime, it.endTime).toMillis() }
        val efficiency = if (asleepMillis > 0) (asleepMillis.toDouble() / timeInBedMillis) * 100.0 else 100.0
        return HealthMetric(type = HealthMetricType.SLEEP_EFFICIENCY, value = efficiency, unit = HealthMetricType.SLEEP_EFFICIENCY.defaultUnit, timestamp = endTime.toEpochMilli())
    }

    private suspend fun readTimeInBed(startTime: Instant, endTime: Instant): HealthMetric? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(recordType = SleepSessionRecord::class, timeRangeFilter = TimeRangeFilter.between(startTime, endTime))
        )
        if (response.records.isEmpty()) return null
        val totalHours = response.records.sumOf { Duration.between(it.startTime, it.endTime).toMillis() } / (1000.0 * 60 * 60)
        return HealthMetric(type = HealthMetricType.TIME_IN_BED_HOURS, value = totalHours, unit = HealthMetricType.TIME_IN_BED_HOURS.defaultUnit, timestamp = endTime.toEpochMilli())
    }
}
