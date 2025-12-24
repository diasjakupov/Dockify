package io.diasjakupov.dockify.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for API error responses.
 */
@Serializable
data class ErrorMessageDto(
    @SerialName("message")
    val message: String
)
