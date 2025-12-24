package io.diasjakupov.dockify.features.location.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for hospital location response.
 * Based on the API, hospitals are returned as Location objects.
 */
@Serializable
data class HospitalDto(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double
)
