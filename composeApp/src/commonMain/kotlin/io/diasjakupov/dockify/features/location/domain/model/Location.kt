package io.diasjakupov.dockify.features.location.domain.model

/**
 * Domain model representing a geographic location.
 */
data class Location(
    val latitude: Double,
    val longitude: Double
) {
    /**
     * Validates that the location coordinates are within valid ranges.
     */
    fun isValid(): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }
}
