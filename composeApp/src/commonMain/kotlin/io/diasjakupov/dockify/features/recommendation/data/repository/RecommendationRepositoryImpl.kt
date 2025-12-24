package io.diasjakupov.dockify.features.recommendation.data.repository

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.recommendation.data.datasource.RecommendationLocalDataSource
import io.diasjakupov.dockify.features.recommendation.data.datasource.RecommendationRemoteDataSource
import io.diasjakupov.dockify.features.recommendation.data.mapper.RecommendationMapper.toDomain
import io.diasjakupov.dockify.features.recommendation.data.mapper.RecommendationMapper.toDto
import io.diasjakupov.dockify.features.recommendation.domain.model.Recommendation
import io.diasjakupov.dockify.features.recommendation.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Implementation of RecommendationRepository.
 * Coordinates between remote and local data sources.
 */
class RecommendationRepositoryImpl(
    private val remoteDataSource: RecommendationRemoteDataSource,
    private val localDataSource: RecommendationLocalDataSource
) : RecommendationRepository {

    override suspend fun getRecommendation(): Resource<Recommendation, DataError> {
        return when (val result = remoteDataSource.getRecommendation()) {
            is Resource.Success -> {
                val recommendation = result.data.toDomain()
                localDataSource.cacheRecommendation(result.data)
                Resource.Success(recommendation)
            }
            is Resource.Error -> {
                // Try to return cached recommendation on error
                val cached = localDataSource.observeCachedRecommendation().first()
                if (cached != null) {
                    Resource.Success(cached.toDomain())
                } else {
                    result
                }
            }
        }
    }

    override fun observeCachedRecommendation(): Flow<Recommendation?> {
        return localDataSource.observeCachedRecommendation().map { dto ->
            dto?.toDomain()
        }
    }

    override suspend fun clearCachedRecommendation() {
        localDataSource.clearCache()
    }
}
