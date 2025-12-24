package io.diasjakupov.dockify.features.recommendation.domain.model

import kotlin.time.Clock
import kotlin.time.ExperimentalTime


/**
 * Domain model representing a health recommendation.
 */
data class Recommendation @OptIn(ExperimentalTime::class) constructor(
    val content: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    /**
     * Checks if the recommendation content is not empty.
     */
    fun isValid(): Boolean = content.isNotBlank()
}
