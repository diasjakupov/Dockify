package io.diasjakupov.dockify.features.recommendation.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.core.network.safeApiCall
import io.diasjakupov.dockify.features.recommendation.data.dto.RecommendationResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get

/**
 * Implementation of RecommendationRemoteDataSource using Ktor HttpClient.
 *
 * @param httpClient The configured Ktor HttpClient
 * @param baseUrl The base URL for the recommendation API
 */
class RecommendationRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : RecommendationRemoteDataSource {

    override suspend fun getRecommendation(): Resource<RecommendationResponseDto, DataError> {
        return safeApiCall {
            httpClient.get("$baseUrl/api/v1/recommendation")
        }
    }
}
