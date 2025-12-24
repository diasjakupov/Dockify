package io.diasjakupov.dockify.features.recommendation.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.recommendation.domain.model.Recommendation
import io.diasjakupov.dockify.features.recommendation.domain.repository.RecommendationRepository

/**
 * Use case for fetching personalized health recommendations.
 */
class GetRecommendationUseCase(
    private val recommendationRepository: RecommendationRepository
) {

    /**
     * Fetches a health recommendation from the backend.
     *
     * @return Resource containing the Recommendation or an error
     */
    suspend operator fun invoke(): Resource<Recommendation, DataError> {
        return recommendationRepository.getRecommendation()
    }
}
