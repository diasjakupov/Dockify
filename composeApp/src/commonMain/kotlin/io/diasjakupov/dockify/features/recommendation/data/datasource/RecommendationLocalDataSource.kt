package io.diasjakupov.dockify.features.recommendation.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.features.recommendation.data.dto.RecommendationResponseDto
import kotlinx.coroutines.flow.Flow

/**
 * Local data source interface for caching recommendations.
 */
interface RecommendationLocalDataSource {

    /**
     * Caches a recommendation locally.
     *
     * @param recommendation The recommendation DTO to cache
     * @return EmptyResult indicating success or a local storage error
     */
    suspend fun cacheRecommendation(recommendation: RecommendationResponseDto): EmptyResult<DataError>

    /**
     * Observes the cached recommendation as a Flow.
     *
     * @return Flow emitting the cached recommendation DTO or null if none exists
     */
    fun observeCachedRecommendation(): Flow<RecommendationResponseDto?>

    /**
     * Clears the cached recommendation.
     *
     * @return EmptyResult indicating success or a local storage error
     */
    suspend fun clearCache(): EmptyResult<DataError>
}
