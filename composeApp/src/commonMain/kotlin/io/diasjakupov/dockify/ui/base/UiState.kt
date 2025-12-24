package io.diasjakupov.dockify.ui.base

/**
 * Base interface for all UI states.
 * States represent the current snapshot of UI data and should be immutable.
 */
interface UiState

/**
 * Common loading states that can be composed into feature states.
 */
enum class LoadingState {
    IDLE,
    LOADING,
    REFRESHING,
    LOADING_MORE
}

/**
 * Mixin interface for states that include loading status.
 */
interface WithLoading {
    val loadingState: LoadingState

    val isLoading: Boolean get() = loadingState == LoadingState.LOADING
    val isRefreshing: Boolean get() = loadingState == LoadingState.REFRESHING
    val isLoadingMore: Boolean get() = loadingState == LoadingState.LOADING_MORE
    val isIdle: Boolean get() = loadingState == LoadingState.IDLE
}

/**
 * Mixin interface for states that can have errors.
 */
interface WithError {
    val error: String?

    val hasError: Boolean get() = error != null
}
