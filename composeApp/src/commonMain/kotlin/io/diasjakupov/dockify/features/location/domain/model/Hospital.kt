package io.diasjakupov.dockify.features.location.domain.model

/**
 * Domain model representing a hospital location.
 */
data class Hospital(
    val location: Location,
    val name: String? = null,
    val address: String? = null
)
