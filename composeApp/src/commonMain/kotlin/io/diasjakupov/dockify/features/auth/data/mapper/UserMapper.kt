package io.diasjakupov.dockify.features.auth.data.mapper

import io.diasjakupov.dockify.features.auth.data.dto.UserLoginRequestDto
import io.diasjakupov.dockify.features.auth.data.dto.UserRegisterRequestDto
import io.diasjakupov.dockify.features.auth.data.dto.UserResponseDto
import io.diasjakupov.dockify.features.auth.domain.model.AuthCredentials
import io.diasjakupov.dockify.features.auth.domain.model.RegistrationData
import io.diasjakupov.dockify.features.auth.domain.model.User

/**
 * Mapper object for converting between auth DTOs and domain models.
 */
object UserMapper {

    /**
     * Converts UserResponseDto to domain User model.
     */
    fun UserResponseDto.toDomain(): User {
        return User(
            id = id.toString(),
            email = email,
            username = username,
            firstName = firstName,
            lastName = lastName
        )
    }

    /**
     * Converts domain AuthCredentials to UserLoginRequestDto.
     */
    fun AuthCredentials.toDto(): UserLoginRequestDto {
        return UserLoginRequestDto(
            email = email,
            password = password
        )
    }

    /**
     * Converts domain RegistrationData to UserRegisterRequestDto.
     */
    fun RegistrationData.toDto(): UserRegisterRequestDto {
        return UserRegisterRequestDto(
            email = email,
            password = password,
            username = username,
            firstName = firstName,
            lastName = lastName
        )
    }
}
