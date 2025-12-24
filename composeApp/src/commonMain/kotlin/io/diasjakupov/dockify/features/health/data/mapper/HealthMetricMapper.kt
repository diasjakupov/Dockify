package io.diasjakupov.dockify.features.health.data.mapper

import io.diasjakupov.dockify.features.health.data.dto.HealthMetricDto
import io.diasjakupov.dockify.features.health.data.dto.HealthMetricsRequestDto
import io.diasjakupov.dockify.features.health.data.dto.LocationDto
import io.diasjakupov.dockify.features.health.domain.model.HealthData
import io.diasjakupov.dockify.features.health.domain.model.HealthMetric
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType
import io.diasjakupov.dockify.features.location.domain.model.Location

/**
 * Mapper object for converting between health DTOs and domain models.
 */
object HealthMetricMapper {

    /**
     * Converts HealthMetricDto to domain HealthMetric model.
     */
    fun HealthMetricDto.toDomain(): HealthMetric? {
        val type = HealthMetricType.fromString(metricType) ?: return null
        val value = metricValue.toDoubleOrNull() ?: return null
        return HealthMetric(
            type = type,
            value = value,
            unit = type.defaultUnit
        )
    }

    /**
     * Converts a list of HealthMetricDto to domain models.
     * Filters out invalid entries.
     */
    fun List<HealthMetricDto>.toDomainList(): List<HealthMetric> {
        return mapNotNull { it.toDomain() }
    }

    /**
     * Converts domain HealthMetric to HealthMetricDto.
     */
    fun HealthMetric.toDto(): HealthMetricDto {
        return HealthMetricDto(
            metricType = type.name,
            metricValue = value.toString()
        )
    }

    /**
     * Converts domain Location to LocationDto.
     */
    fun Location.toDto(): LocationDto {
        return LocationDto(
            latitude = latitude,
            longitude = longitude
        )
    }

    /**
     * Converts LocationDto to domain Location.
     */
    fun LocationDto.toDomain(): Location {
        return Location(
            latitude = latitude,
            longitude = longitude
        )
    }

    /**
     * Converts domain HealthData to HealthMetricsRequestDto.
     */
    fun HealthData.toDto(): HealthMetricsRequestDto {
        return HealthMetricsRequestDto(
            userId = userId.toIntOrNull() ?: 0,
            metrics = metrics.map { it.toDto() },
            location = location?.toDto()
        )
    }
}
