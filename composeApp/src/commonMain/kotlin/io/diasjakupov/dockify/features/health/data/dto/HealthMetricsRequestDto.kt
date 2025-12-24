package io.diasjakupov.dockify.features.health.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for creating health metrics request.
 */
@Serializable
data class HealthMetricsRequestDto(
    @SerialName("user_id")
    val userId: Int,
    @SerialName("metrics")
    val metrics: List<HealthMetricDto>,
    @SerialName("location")
    val location: LocationDto? = null
)
