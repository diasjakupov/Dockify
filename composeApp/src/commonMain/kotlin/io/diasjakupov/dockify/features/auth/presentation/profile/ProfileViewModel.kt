package io.diasjakupov.dockify.features.auth.presentation.profile

import io.diasjakupov.dockify.core.demo.DemoModeRepository
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.domain.usecase.GetCurrentUserUseCase
import io.diasjakupov.dockify.features.auth.domain.usecase.LogoutUseCase
import io.diasjakupov.dockify.ui.base.BaseViewModel
import io.diasjakupov.dockify.ui.base.LoadingState

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val demoModeRepository: DemoModeRepository
) : BaseViewModel<ProfileState, ProfileAction, ProfileEffect>(ProfileState()) {

    init {
        onAction(ProfileAction.LoadProfile)
        observeDemoMode()
    }

    override fun handleAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.LoadProfile -> loadProfile()
            is ProfileAction.Logout -> logout()
            is ProfileAction.ToggleDemoMode -> toggleDemoMode()
        }
    }

    private fun observeDemoMode() {
        collectFlow(demoModeRepository.isDemoMode()) { isDemoMode ->
            copy(isDemoMode = isDemoMode)
        }
    }

    private fun toggleDemoMode() {
        launch {
            demoModeRepository.setDemoMode(!currentState.isDemoMode)
        }
    }

    private fun loadProfile() {
        updateState { copy(loadingState = LoadingState.LOADING) }
        launch {
            when (val result = getCurrentUserUseCase()) {
                is Resource.Success -> updateState {
                    copy(user = result.data, loadingState = LoadingState.IDLE)
                }
                is Resource.Error -> updateState {
                    copy(error = "Failed to load profile", loadingState = LoadingState.IDLE)
                }
            }
        }
    }

    private fun logout() {
        launch {
            logoutUseCase()
            emitEffect(ProfileEffect.NavigateToLogin)
        }
    }
}
