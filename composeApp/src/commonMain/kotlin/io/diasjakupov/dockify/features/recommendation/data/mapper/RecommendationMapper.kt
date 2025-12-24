package io.diasjakupov.dockify.features.recommendation.data.mapper

import io.diasjakupov.dockify.features.recommendation.data.dto.RecommendationResponseDto
import io.diasjakupov.dockify.features.recommendation.domain.model.Recommendation

/**
 * Mapper object for converting between recommendation DTOs and domain models.
 */
object RecommendationMapper {

    /**
     * Converts RecommendationResponseDto to domain Recommendation model.
     */
    fun RecommendationResponseDto.toDomain(): Recommendation {
        return Recommendation(
            content = recommendation
        )
    }

    /**
     * Converts domain Recommendation to RecommendationResponseDto.
     */
    fun Recommendation.toDto(): RecommendationResponseDto {
        return RecommendationResponseDto(
            recommendation = content
        )
    }
}
