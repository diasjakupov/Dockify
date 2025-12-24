package io.diasjakupov.dockify.features.location.data.mapper

import io.diasjakupov.dockify.features.health.data.dto.LocationDto
import io.diasjakupov.dockify.features.location.data.dto.HospitalDto
import io.diasjakupov.dockify.features.location.data.dto.NearestHospitalsRequestDto
import io.diasjakupov.dockify.features.location.data.dto.NearestUsersRequestDto
import io.diasjakupov.dockify.features.location.data.dto.NearestUsersResponseDto
import io.diasjakupov.dockify.features.location.domain.model.Hospital
import io.diasjakupov.dockify.features.location.domain.model.Location
import io.diasjakupov.dockify.features.location.domain.model.NearbyUser

/**
 * Mapper object for converting between location DTOs and domain models.
 */
object LocationMapper {

    /**
     * Converts NearestUsersResponseDto to domain NearbyUser model.
     */
    fun NearestUsersResponseDto.toDomain(): NearbyUser {
        return NearbyUser(
            userId = userId.toString(),
            location = Location(
                latitude = location.latitude,
                longitude = location.longitude
            )
        )
    }

    /**
     * Converts a list of NearestUsersResponseDto to domain models.
     */
    fun List<NearestUsersResponseDto>.toDomainUsers(): List<NearbyUser> {
        return map { it.toDomain() }
    }

    /**
     * Converts HospitalDto to domain Hospital model.
     */
    fun HospitalDto.toDomain(): Hospital {
        return Hospital(
            location = Location(
                latitude = latitude,
                longitude = longitude
            )
        )
    }

    /**
     * Converts a list of HospitalDto to domain models.
     */
    fun List<HospitalDto>.toDomainHospitals(): List<Hospital> {
        return map { it.toDomain() }
    }

    /**
     * Creates a NearestUsersRequestDto from domain parameters.
     */
    fun createNearestUsersRequest(
        location: Location,
        radiusMeters: Double,
        userId: String
    ): NearestUsersRequestDto {
        return NearestUsersRequestDto(
            latitude = location.latitude,
            longitude = location.longitude,
            radius = radiusMeters.toInt(),
            userId = userId.toIntOrNull() ?: 0
        )
    }

    /**
     * Creates a NearestHospitalsRequestDto from domain parameters.
     */
    fun createNearestHospitalsRequest(
        location: Location,
        radiusMeters: Double
    ): NearestHospitalsRequestDto {
        return NearestHospitalsRequestDto(
            latitude = location.latitude,
            longitude = location.longitude,
            radius = radiusMeters.toInt()
        )
    }

    /**
     * Converts domain Location to LocationDto.
     */
    fun Location.toDto(): LocationDto {
        return LocationDto(
            latitude = latitude,
            longitude = longitude
        )
    }

    /**
     * Converts LocationDto to domain Location.
     */
    fun LocationDto.toDomain(): Location {
        return Location(
            latitude = latitude,
            longitude = longitude
        )
    }
}
