package io.diasjakupov.dockify.features.location.data.dto

import io.diasjakupov.dockify.features.health.data.dto.LocationDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for nearest users response.
 */
@Serializable
data class NearestUsersResponseDto(
    @SerialName("user_id")
    val userId: Int,
    @SerialName("location")
    val location: LocationDto
)
