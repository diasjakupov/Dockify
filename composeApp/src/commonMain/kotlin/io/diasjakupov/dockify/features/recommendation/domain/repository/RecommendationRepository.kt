package io.diasjakupov.dockify.features.recommendation.domain.repository

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.recommendation.domain.model.Recommendation
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for recommendation operations.
 * Domain layer defines this contract; data layer provides implementation.
 */
interface RecommendationRepository {

    /**
     * Fetches a health recommendation from the backend.
     *
     * @return Resource containing the Recommendation or an error
     */
    suspend fun getRecommendation(): Resource<Recommendation, DataError>

    /**
     * Observes cached recommendation from local storage.
     *
     * @return Flow emitting the cached recommendation or null if none exists
     */
    fun observeCachedRecommendation(): Flow<Recommendation?>

    /**
     * Clears the cached recommendation.
     */
    suspend fun clearCachedRecommendation()
}
