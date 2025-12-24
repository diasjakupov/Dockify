package io.diasjakupov.dockify.features.location.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for nearest hospitals request.
 */
@Serializable
data class NearestHospitalsRequestDto(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("radius")
    val radius: Int
)
