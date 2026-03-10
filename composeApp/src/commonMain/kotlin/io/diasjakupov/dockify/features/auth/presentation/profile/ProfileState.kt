package io.diasjakupov.dockify.features.auth.presentation.profile

import io.diasjakupov.dockify.features.auth.domain.model.User
import io.diasjakupov.dockify.ui.base.LoadingState
import io.diasjakupov.dockify.ui.base.UiAction
import io.diasjakupov.dockify.ui.base.UiEffect
import io.diasjakupov.dockify.ui.base.UiState
import io.diasjakupov.dockify.ui.base.WithError
import io.diasjakupov.dockify.ui.base.WithLoading

data class ProfileState(
    val user: User? = null,
    override val loadingState: LoadingState = LoadingState.IDLE,
    override val error: String? = null
) : UiState, WithLoading, WithError

sealed interface ProfileAction : UiAction {
    data object LoadProfile : ProfileAction
    data object Logout : ProfileAction
}

sealed interface ProfileEffect : UiEffect {
    data object NavigateToLogin : ProfileEffect
}
