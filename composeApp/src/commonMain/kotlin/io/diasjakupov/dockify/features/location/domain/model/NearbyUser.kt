package io.diasjakupov.dockify.features.location.domain.model

/**
 * Domain model representing a nearby user with their location.
 */
data class NearbyUser(
    val userId: String,
    val location: Location
)
