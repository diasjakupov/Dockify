package io.diasjakupov.dockify.features.auth.domain.model

/**
 * Domain model representing user registration data.
 */
data class RegistrationData(
    val email: String,
    val password: String,
    val username: String,
    val firstName: String,
    val lastName: String
)
