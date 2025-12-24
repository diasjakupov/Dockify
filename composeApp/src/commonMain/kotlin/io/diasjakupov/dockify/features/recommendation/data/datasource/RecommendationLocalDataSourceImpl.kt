package io.diasjakupov.dockify.features.recommendation.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.recommendation.data.dto.RecommendationResponseDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory implementation of RecommendationLocalDataSource.
 * Caches the latest recommendation for quick access.
 */
class RecommendationLocalDataSourceImpl : RecommendationLocalDataSource {

    private val _cachedRecommendation = MutableStateFlow<RecommendationResponseDto?>(null)

    override suspend fun cacheRecommendation(recommendation: RecommendationResponseDto): EmptyResult<DataError> {
        _cachedRecommendation.value = recommendation
        return Resource.Success(Unit)
    }

    override fun observeCachedRecommendation(): Flow<RecommendationResponseDto?> {
        return _cachedRecommendation.asStateFlow()
    }

    override suspend fun clearCache(): EmptyResult<DataError> {
        _cachedRecommendation.value = null
        return Resource.Success(Unit)
    }
}
