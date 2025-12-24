package io.diasjakupov.dockify.ui.base

import io.diasjakupov.dockify.core.domain.DataError

/**
 * Maps DataError to user-friendly error messages.
 */
fun DataError.toUserMessage(): String {
    return when (this) {
        // Network errors
        DataError.Network.NO_INTERNET -> "No internet connection. Please check your network."
        DataError.Network.REQUEST_TIMEOUT -> "Request timed out. Please try again."
        DataError.Network.SERVER_ERROR -> "Server error. Please try again later."
        DataError.Network.SERIALIZATION_ERROR -> "Unable to process data."
        DataError.Network.UNKNOWN -> "An unexpected error occurred."

        // Auth errors
        DataError.Auth.INVALID_CREDENTIALS -> "Invalid email or password."
        DataError.Auth.USER_NOT_FOUND -> "User not found."
        DataError.Auth.USER_ALREADY_EXISTS -> "An account with this email already exists."
        DataError.Auth.SESSION_EXPIRED -> "Your session has expired. Please log in again."
        DataError.Auth.INVALID_TOKEN -> "Authentication error. Please log in again."
        DataError.Auth.UNAUTHORIZED -> "You are not authorized to perform this action."

        // Health errors
        DataError.Health.PERMISSION_DENIED -> "Health data access denied. Please enable permissions."
        DataError.Health.HEALTH_CONNECT_NOT_AVAILABLE -> "Health Connect is not available on this device."
        DataError.Health.HEALTHKIT_NOT_AVAILABLE -> "HealthKit is not available on this device."
        DataError.Health.DATA_NOT_FOUND -> "No health data found."
        DataError.Health.SYNC_FAILED -> "Failed to sync health data."
        DataError.Health.INVALID_DATA_FORMAT -> "Invalid health data format."

        // Location errors
        DataError.Location.PERMISSION_DENIED -> "Location permission denied."
        DataError.Location.GPS_DISABLED -> "GPS is disabled. Please enable location services."
        DataError.Location.LOCATION_UNAVAILABLE -> "Unable to determine your location."
        DataError.Location.TIMEOUT -> "Location request timed out."

        // Local storage errors
        DataError.Local.STORAGE_FULL -> "Device storage is full."
        DataError.Local.READ_ERROR -> "Unable to read data from storage."
        DataError.Local.WRITE_ERROR -> "Unable to save data."
        DataError.Local.NOT_FOUND -> "Data not found."

        else -> "An unexpected error occurred."
    }
}
