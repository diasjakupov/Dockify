package io.diasjakupov.dockify.ui.base

/**
 * Base interface for all UI effects (one-time events).
 * Effects are side effects that should be consumed only once
 * (e.g., navigation, showing snackbars, toasts).
 */
interface UiEffect

/**
 * Common navigation effects.
 */
sealed interface NavigationEffect : UiEffect {
    data object NavigateBack : NavigationEffect
    data class NavigateTo(val route: String) : NavigationEffect
    data class NavigateToAndClearStack(val route: String) : NavigationEffect
}

/**
 * Common message effects for showing feedback to users.
 */
sealed interface MessageEffect : UiEffect {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val duration: SnackbarDuration = SnackbarDuration.Short
    ) : MessageEffect

    data class ShowToast(val message: String) : MessageEffect

    data class ShowDialog(
        val title: String,
        val message: String,
        val confirmLabel: String = "OK",
        val dismissLabel: String? = null
    ) : MessageEffect
}

enum class SnackbarDuration {
    Short,
    Long,
    Indefinite
}
