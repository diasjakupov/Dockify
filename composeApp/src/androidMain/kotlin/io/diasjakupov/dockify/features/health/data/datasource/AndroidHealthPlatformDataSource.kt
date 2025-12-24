package io.diasjakupov.dockify.features.health.data.datasource

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
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
}
