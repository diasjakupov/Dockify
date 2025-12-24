package io.diasjakupov.dockify.features.health.presentation

import io.diasjakupov.dockify.ui.base.UiAction

/**
 * Actions that can be triggered from the Health screen.
 */
sealed interface HealthAction : UiAction {
    /** Load initial health data from backend */
    data object LoadHealthData : HealthAction

    /** Sync health data from device to backend */
    data object SyncHealthData : HealthAction

    /** Request health permissions from the platform */
    data object RequestPermissions : HealthAction

    /** Called when permissions are granted */
    data object PermissionGranted : HealthAction

    /** Called when permissions are denied */
    data object PermissionDenied : HealthAction

    /** Load/refresh the AI recommendation */
    data object RefreshRecommendation : HealthAction

    /** Dismiss the current error */
    data object DismissError : HealthAction

    /** Retry the last failed action */
    data object RetryLastAction : HealthAction

    /** Check permissions and auto-sync on screen load */
    data object CheckPermissionsAndAutoSync : HealthAction

    /** Dismiss background sync error indicator */
    data object DismissBackgroundSyncError : HealthAction

    /** Retry background sync only (without full reload) */
    data object RetryBackgroundSync : HealthAction
}
