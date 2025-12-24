package io.diasjakupov.dockify.features.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for user login request.
 */
@Serializable
data class UserLoginRequestDto(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String
)
