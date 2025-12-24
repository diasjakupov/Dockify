package io.diasjakupov.dockify.features.health.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.core.network.safeApiCall
import io.diasjakupov.dockify.core.network.safeApiCallEmpty
import io.diasjakupov.dockify.features.health.data.dto.HealthMetricDto
import io.diasjakupov.dockify.features.health.data.dto.HealthMetricsRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

/**
 * Implementation of HealthRemoteDataSource using Ktor HttpClient.
 *
 * @param httpClient The configured Ktor HttpClient
 * @param baseUrl The base URL for the health API
 */
class HealthRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : HealthRemoteDataSource {

    override suspend fun getHealthMetrics(userId: String): Resource<List<HealthMetricDto>, DataError> {
        return safeApiCall {
            httpClient.get("$baseUrl/api/v1/metrics") {
                url {
                    parameters.append("user_id", userId)
                }
            }
        }
    }

    override suspend fun createHealthMetrics(request: HealthMetricsRequestDto): EmptyResult<DataError> {
        return safeApiCallEmpty {
            httpClient.post("$baseUrl/api/v1/metrics") {
                setBody(request)
            }
        }
    }
}
