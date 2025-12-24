package io.diasjakupov.dockify.features.recommendation.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for recommendation response.
 */
@Serializable
data class RecommendationResponseDto(
    @SerialName("recommendation")
    val recommendation: String
)
