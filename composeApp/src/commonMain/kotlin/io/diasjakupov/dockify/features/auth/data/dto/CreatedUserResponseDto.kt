package io.diasjakupov.dockify.features.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for created user response from registration.
 */
@Serializable
data class CreatedUserResponseDto(
    @SerialName("user_id")
    val userId: Int
)
