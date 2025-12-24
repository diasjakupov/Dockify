package io.diasjakupov.dockify.features.health.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for a single health metric.
 */
@Serializable
data class HealthMetricDto(
    @SerialName("metric_type")
    val metricType: String,
    @SerialName("metric_value")
    val metricValue: String
)
