package io.diasjakupov.dockify.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Abstract base ViewModel implementing the MVI pattern with State, Action, and Effect.
 *
 * @param S The UI state type
 * @param A The action (intent) type
 * @param E The effect (one-time event) type
 */
abstract class BaseViewModel<S : UiState, A : UiAction, E : UiEffect>(
    initialState: S
) : ViewModel() {

    /**
     * Internal mutable state flow.
     */
    private val _state = MutableStateFlow(initialState)

    /**
     * Public immutable state flow that UI observes.
     */
    val state: StateFlow<S> = _state.asStateFlow()

    /**
     * Current state value (for convenience in ViewModel logic).
     */
    protected val currentState: S get() = _state.value

    /**
     * Channel for one-time effects (navigation, snackbars, etc.).
     * Using Channel ensures effects are consumed exactly once.
     */
    private val _effect = Channel<E>(Channel.BUFFERED)

    /**
     * Flow of effects that UI collects.
     */
    val effect: Flow<E> = _effect.receiveAsFlow()

    /**
     * Processes incoming actions and updates state/emits effects accordingly.
     * Must be implemented by concrete ViewModels.
     *
     * @param action The action to process
     */
    protected abstract fun handleAction(action: A)

    /**
     * Called by UI to dispatch actions to the ViewModel.
     *
     * @param action The action triggered by user interaction or system event
     */
    fun onAction(action: A) {
        handleAction(action)
    }

    /**
     * Updates the UI state using a reducer function.
     * Ensures thread-safe state updates.
     *
     * @param reducer Function that takes current state and returns new state
     */
    protected fun updateState(reducer: S.() -> S) {
        _state.update { it.reducer() }
    }

    /**
     * Emits a one-time effect to be consumed by the UI.
     *
     * @param effect The effect to emit
     */
    protected fun emitEffect(effect: E) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    /**
     * Launches a coroutine in the ViewModel scope with common error handling.
     *
     * @param onError Optional error handler
     * @param block The suspend function to execute
     */
    protected fun launch(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Throwable) {
                onError?.invoke(e)
            }
        }
    }

    /**
     * Collects a flow and updates state with each emission.
     *
     * @param flow The flow to collect
     * @param reducer Function to update state with each emission
     */
    protected fun <T> collectFlow(
        flow: Flow<T>,
        reducer: S.(T) -> S
    ) {
        viewModelScope.launch {
            flow.collect { value ->
                updateState { reducer(value) }
            }
        }
    }
}
