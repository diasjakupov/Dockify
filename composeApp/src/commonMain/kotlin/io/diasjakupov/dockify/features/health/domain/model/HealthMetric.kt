package io.diasjakupov.dockify.features.health.domain.model

/**
 * Domain model representing a single health metric.
 */
data class HealthMetric(
    val type: HealthMetricType,
    val value: Double,
    val unit: String,
    val timestamp: Long? = null
)
