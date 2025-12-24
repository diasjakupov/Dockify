package io.diasjakupov.dockify.features.auth.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.data.dto.CreatedUserResponseDto
import io.diasjakupov.dockify.features.auth.data.dto.LoginResponseDto
import io.diasjakupov.dockify.features.auth.data.dto.UserLoginRequestDto
import io.diasjakupov.dockify.features.auth.data.dto.UserRegisterRequestDto
import io.diasjakupov.dockify.features.auth.data.dto.UserResponseDto

/**
 * Remote data source interface for authentication API calls.
 */
interface AuthRemoteDataSource {

    /**
     * Performs login API call.
     *
     * @param request The login request DTO
     * @return Resource containing the user response or a network/auth error
     */
    suspend fun login(request: UserLoginRequestDto): Resource<LoginResponseDto, DataError>

    /**
     * Performs registration API call.
     *
     * @param request The registration request DTO
     * @return Resource containing the created user response or an error
     */
    suspend fun register(request: UserRegisterRequestDto): Resource<CreatedUserResponseDto, DataError>
}
