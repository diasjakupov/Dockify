package io.diasjakupov.dockify.features.auth.domain.model

/**
 * Domain model representing an authenticated user.
 */
data class User(
    val id: String,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}
