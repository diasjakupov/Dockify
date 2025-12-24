package io.diasjakupov.dockify.features.health.domain.model

import io.diasjakupov.dockify.features.location.domain.model.Location
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Domain model representing health data to be synced with the backend.
 */
data class HealthData @OptIn(ExperimentalTime::class) constructor(
    val userId: String,
    val metrics: List<HealthMetric>,
    val location: Location? = null,
    val syncTimestamp: Long = Clock.System.now().toEpochMilliseconds()
)
