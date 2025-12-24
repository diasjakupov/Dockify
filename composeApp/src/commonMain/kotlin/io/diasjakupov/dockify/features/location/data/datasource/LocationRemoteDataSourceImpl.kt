package io.diasjakupov.dockify.features.location.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.core.network.safeApiCall
import io.diasjakupov.dockify.features.location.data.dto.HospitalDto
import io.diasjakupov.dockify.features.location.data.dto.NearestHospitalsRequestDto
import io.diasjakupov.dockify.features.location.data.dto.NearestUsersRequestDto
import io.diasjakupov.dockify.features.location.data.dto.NearestUsersResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody

/**
 * Implementation of LocationRemoteDataSource using Ktor HttpClient.
 *
 * @param httpClient The configured Ktor HttpClient
 * @param baseUrl The base URL for the location API
 */
class LocationRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : LocationRemoteDataSource {

    override suspend fun getNearestUsers(
        request: NearestUsersRequestDto
    ): Resource<List<NearestUsersResponseDto>, DataError> {
        return safeApiCall {
            httpClient.post("$baseUrl/api/v1/location/nearest") {
                setBody(request)
            }
        }
    }

    override suspend fun getNearestHospitals(
        request: NearestHospitalsRequestDto
    ): Resource<List<HospitalDto>, DataError> {
        return safeApiCall {
            httpClient.post("$baseUrl/api/v1/location/hospitals") {
                setBody(request)
            }
        }
    }
}
