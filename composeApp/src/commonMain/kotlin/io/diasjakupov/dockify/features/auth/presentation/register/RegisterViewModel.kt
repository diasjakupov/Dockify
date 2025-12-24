package io.diasjakupov.dockify.features.auth.presentation.register

import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.domain.usecase.RegisterUseCase
import io.diasjakupov.dockify.ui.base.BaseViewModel
import io.diasjakupov.dockify.ui.base.LoadingState
import io.diasjakupov.dockify.ui.base.toUserMessage

private const val TAG = "RegisterViewModel"

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : BaseViewModel<RegisterState, RegisterAction, RegisterEffect>(RegisterState()) {

    override fun handleAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.EmailChanged -> handleEmailChanged(action.email)
            is RegisterAction.PasswordChanged -> handlePasswordChanged(action.password)
            is RegisterAction.ConfirmPasswordChanged -> handleConfirmPasswordChanged(action.confirmPassword)
            is RegisterAction.UsernameChanged -> handleUsernameChanged(action.username)
            is RegisterAction.FirstNameChanged -> updateState { copy(firstName = action.firstName) }
            is RegisterAction.LastNameChanged -> updateState { copy(lastName = action.lastName) }
            is RegisterAction.RegisterClicked -> handleRegister()
            is RegisterAction.LoginClicked -> emitEffect(RegisterEffect.NavigateToLogin)
            is RegisterAction.ErrorDismissed -> updateState { copy(error = null) }
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
        val confirmPasswordError = if (currentState.confirmPassword.isNotEmpty()) {
            validateConfirmPassword(currentState.confirmPassword, password)
        } else null

        updateState {
            copy(
                password = password,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                error = null
            )
        }
    }

    private fun handleConfirmPasswordChanged(confirmPassword: String) {
        val confirmPasswordError = validateConfirmPassword(confirmPassword, currentState.password)
        updateState {
            copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = confirmPasswordError,
                error = null
            )
        }
    }

    private fun handleUsernameChanged(username: String) {
        val usernameError = validateUsername(username)
        updateState {
            copy(
                username = username,
                usernameError = usernameError,
                error = null
            )
        }
    }

    private fun handleRegister() {
        println("$TAG: handleRegister called")
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)
        val confirmPasswordError = validateConfirmPassword(currentState.confirmPassword, currentState.password)
        val usernameError = validateUsername(currentState.username)

        println("$TAG: Validation - email: $emailError, password: $passwordError, confirmPassword: $confirmPasswordError, username: $usernameError")

        if (emailError != null || passwordError != null || confirmPasswordError != null || usernameError != null) {
            println("$TAG: Validation failed, not proceeding with registration")
            updateState {
                copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
                    usernameError = usernameError
                )
            }
            return
        }

        println("$TAG: Validation passed, starting registration")
        println("$TAG: Email: ${currentState.email}, Username: ${currentState.username}")
        updateState { copy(loadingState = LoadingState.LOADING) }

        launch(
            onError = { error ->
                println("$TAG: Launch error - ${error::class.simpleName}: ${error.message}")
                error.printStackTrace()
                updateState {
                    copy(
                        loadingState = LoadingState.IDLE,
                        error = error.message ?: "An unexpected error occurred"
                    )
                }
            }
        ) {
            println("$TAG: Calling registerUseCase")
            val result = registerUseCase(
                email = currentState.email,
                password = currentState.password,
                username = currentState.username,
                firstName = currentState.firstName,
                lastName = currentState.lastName
            )

            println("$TAG: registerUseCase returned: $result")

            when (result) {
                is Resource.Success -> {
                    println("$TAG: Registration successful - userId: ${result.data}")
                    updateState { copy(loadingState = LoadingState.IDLE) }
                    emitEffect(RegisterEffect.ShowSuccessMessage("Registration successful! Please login."))
                    emitEffect(RegisterEffect.NavigateToLogin)
                }
                is Resource.Error -> {
                    println("$TAG: Registration failed - error: ${result.error}")
                    val errorMessage = result.error.toUserMessage()
                    println("$TAG: Error message: $errorMessage")
                    updateState {
                        copy(
                            loadingState = LoadingState.IDLE,
                            error = errorMessage
                        )
                    }
                    emitEffect(RegisterEffect.ShowSnackbar(errorMessage))
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
            password.length < RegisterState.MIN_PASSWORD_LENGTH ->
                "Password must be at least ${RegisterState.MIN_PASSWORD_LENGTH} characters"
            else -> null
        }
    }

    private fun validateConfirmPassword(confirmPassword: String, password: String): String? {
        return when {
            confirmPassword.isBlank() -> "Please confirm your password"
            confirmPassword != password -> "Passwords do not match"
            else -> null
        }
    }

    private fun validateUsername(username: String): String? {
        return when {
            username.isBlank() -> "Username is required"
            else -> null
        }
    }
}
