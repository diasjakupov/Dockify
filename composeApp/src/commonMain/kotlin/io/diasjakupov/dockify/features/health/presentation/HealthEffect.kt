package io.diasjakupov.dockify.features.health.presentation

import io.diasjakupov.dockify.ui.base.UiEffect

/**
 * One-time effects emitted by the Health ViewModel.
 */
sealed interface HealthEffect : UiEffect {
    /** Show a snackbar message */
    data class ShowSnackbar(val message: String) : HealthEffect

    /** Notify that sync completed successfully */
    data object SyncSuccess : HealthEffect

    /** Notify that background sync failed (subtle notification) */
    data object BackgroundSyncFailed : HealthEffect
}
