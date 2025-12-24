package io.diasjakupov.dockify.features.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for user registration request.
 */
@Serializable
data class UserRegisterRequestDto(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String,
    @SerialName("username")
    val username: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String
)
