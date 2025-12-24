package io.diasjakupov.dockify.features.location.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.location.data.dto.HospitalDto
import io.diasjakupov.dockify.features.location.data.dto.NearestHospitalsRequestDto
import io.diasjakupov.dockify.features.location.data.dto.NearestUsersRequestDto
import io.diasjakupov.dockify.features.location.data.dto.NearestUsersResponseDto

/**
 * Remote data source interface for location-related API calls.
 */
interface LocationRemoteDataSource {

    /**
     * Fetches nearest users from the backend.
     *
     * @param request The nearest users request DTO
     * @return Resource containing the list of nearby user DTOs or an error
     */
    suspend fun getNearestUsers(
        request: NearestUsersRequestDto
    ): Resource<List<NearestUsersResponseDto>, DataError>

    /**
     * Fetches nearest hospitals from the backend.
     *
     * @param request The nearest hospitals request DTO
     * @return Resource containing the list of hospital DTOs or an error
     */
    suspend fun getNearestHospitals(
        request: NearestHospitalsRequestDto
    ): Resource<List<HospitalDto>, DataError>
}
