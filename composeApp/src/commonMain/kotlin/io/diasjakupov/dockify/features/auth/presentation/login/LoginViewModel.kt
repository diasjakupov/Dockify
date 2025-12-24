package io.diasjakupov.dockify.features.auth.presentation.login

import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.domain.usecase.LoginUseCase
import io.diasjakupov.dockify.ui.base.BaseViewModel
import io.diasjakupov.dockify.ui.base.LoadingState
import io.diasjakupov.dockify.ui.base.toUserMessage

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : BaseViewModel<LoginState, LoginAction, LoginEffect>(LoginState()) {

    override fun handleAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> handleEmailChanged(action.email)
            is LoginAction.PasswordChanged -> handlePasswordChanged(action.password)
            is LoginAction.LoginClicked -> handleLogin()
            is LoginAction.RegisterClicked -> emitEffect(LoginEffect.NavigateToRegister)
            is LoginAction.ForgotPasswordClicked -> emitEffect(LoginEffect.NavigateToForgotPassword)
            is LoginAction.ErrorDismissed -> updateState { copy(error = null) }
        }
    }

    private fun handleEmailChanged(email: String) {
        val emailError = validateEmail(email)
        updateState {
            copy(
                email = email,
                emailError = emailError,
                error = null
            )
        }
    }

    private fun handlePasswordChanged(password: String) {
        val passwordError = validatePassword(password)
        updateState {
            copy(
                password = password,
                passwordError = passwordError,
                error = null
            )
        }
    }

    private fun handleLogin() {
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)

        if (emailError != null || passwordError != null) {
            updateState {
                copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        updateState { copy(loadingState = LoadingState.LOADING) }

        launch(
            onError = { error ->
                updateState {
                    copy(
                        loadingState = LoadingState.IDLE,
                        error = error.message ?: "An unexpected error occurred"
                    )
                }
            }
        ) {
            when (val result = loginUseCase(currentState.email, currentState.password)) {
                is Resource.Success -> {
                    updateState { copy(loadingState = LoadingState.IDLE) }
                    emitEffect(LoginEffect.NavigateToHome)
                }
                is Resource.Error -> {
                    val errorMessage = result.error.toUserMessage()
                    updateState {
                        copy(
                            loadingState = LoadingState.IDLE,
                            error = errorMessage
                        )
                    }
                    emitEffect(LoginEffect.ShowSnackbar(errorMessage))
                }
            }
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !email.contains("@") || !email.contains(".") -> "Please enter a valid email"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            else -> null
        }
    }
}
