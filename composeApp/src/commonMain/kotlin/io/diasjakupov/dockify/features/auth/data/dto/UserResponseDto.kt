package io.diasjakupov.dockify.features.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wrapper DTO for login response.
 * The API returns user data wrapped in a "user" object.
 */
@Serializable
data class LoginResponseDto(
    @SerialName("user")
    val user: UserResponseDto
)

/**
 * DTO for user data from API response.
 */
@Serializable
data class UserResponseDto(
    @SerialName("id")
    val id: Int,
    @SerialName("username")
    val username: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    @SerialName("email")
    val email: String,
    @SerialName("created_at")
    val createdAt: String
)
