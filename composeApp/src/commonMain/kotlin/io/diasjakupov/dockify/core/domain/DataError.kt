package io.diasjakupov.dockify.core.domain

/**
 * Sealed interface representing all possible data errors in the application.
 * Organized by error category for better error handling and user feedback.
 */
sealed interface DataError : Error {

    /**
     * Network-related errors that occur during API communication.
     */
    enum class Network : DataError {
        NO_INTERNET,
        REQUEST_TIMEOUT,
        SERVER_ERROR,
        SERIALIZATION_ERROR,
        UNKNOWN
    }

    /**
     * Authentication-related errors.
     */
    enum class Auth : DataError {
        INVALID_CREDENTIALS,
        USER_NOT_FOUND,
        USER_ALREADY_EXISTS,
        SESSION_EXPIRED,
        INVALID_TOKEN,
        UNAUTHORIZED
    }

    /**
     * Health data-related errors (Health Connect / HealthKit).
     */
    enum class Health : DataError {
        PERMISSION_DENIED,
        HEALTH_CONNECT_NOT_AVAILABLE,
        HEALTHKIT_NOT_AVAILABLE,
        DATA_NOT_FOUND,
        SYNC_FAILED,
        INVALID_DATA_FORMAT
    }

    /**
     * Location-related errors.
     */
    enum class Location : DataError {
        PERMISSION_DENIED,
        GPS_DISABLED,
        LOCATION_UNAVAILABLE,
        TIMEOUT
    }

    /**
     * Local storage errors (DataStore, caching).
     */
    enum class Local : DataError {
        STORAGE_FULL,
        READ_ERROR,
        WRITE_ERROR,
        NOT_FOUND
    }
}
