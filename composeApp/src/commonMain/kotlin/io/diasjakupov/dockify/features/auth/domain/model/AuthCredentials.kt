package io.diasjakupov.dockify.features.auth.domain.model

/**
 * Domain model representing login credentials.
 */
data class AuthCredentials(
    val email: String,
    val password: String
)
