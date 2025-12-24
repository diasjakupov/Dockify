package io.diasjakupov.dockify.features.location.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for nearest users request.
 */
@Serializable
data class NearestUsersRequestDto(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("radius")
    val radius: Int,
    @SerialName("user_id")
    val userId: Int
)
