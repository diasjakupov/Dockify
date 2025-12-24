package io.diasjakupov.dockify.features.recommendation.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.recommendation.data.dto.RecommendationResponseDto

/**
 * Remote data source interface for recommendation API calls.
 */
interface RecommendationRemoteDataSource {

    /**
     * Fetches a recommendation from the backend.
     *
     * @return Resource containing the recommendation response DTO or an error
     */
    suspend fun getRecommendation(): Resource<RecommendationResponseDto, DataError>
}
