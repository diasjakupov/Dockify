package io.diasjakupov.dockify.ui.base

/**
 * Base interface for all UI actions (user intents).
 * Actions represent user interactions or system events that trigger state changes.
 */
interface UiAction

/**
 * Common lifecycle actions that can be reused across features.
 */
sealed interface LifecycleAction : UiAction {
    data object OnCreate : LifecycleAction
    data object OnResume : LifecycleAction
    data object OnPause : LifecycleAction
}

/**
 * Common data loading actions.
 */
sealed interface DataAction : UiAction {
    data object Refresh : DataAction
    data object LoadMore : DataAction
    data object Retry : DataAction
}
