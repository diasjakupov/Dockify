package io.diasjakupov.dockify.features.auth.presentation.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.features.auth.presentation.components.AuthTextField
import io.diasjakupov.dockify.features.auth.presentation.components.PasswordTextField
import io.diasjakupov.dockify.ui.components.common.DockifyScaffold
import io.diasjakupov.dockify.ui.components.common.TopBarConfig
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RegisterEffect.NavigateToLogin -> onNavigateToLogin()
                is RegisterEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is RegisterEffect.ShowSuccessMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    DockifyScaffold(
        topBarConfig = TopBarConfig.Simple(
            title = "Create Account",
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        ),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Join Dockify",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Create an account to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                AuthTextField(
                    value = state.email,
                    onValueChange = { viewModel.onAction(RegisterAction.EmailChanged(it)) },
                    label = "Email",
                    error = state.emailError,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    value = state.username,
                    onValueChange = { viewModel.onAction(RegisterAction.UsernameChanged(it)) },
                    label = "Username",
                    error = state.usernameError,
                    imeAction = ImeAction.Next,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    value = state.firstName,
                    onValueChange = { viewModel.onAction(RegisterAction.FirstNameChanged(it)) },
                    label = "First Name (optional)",
                    imeAction = ImeAction.Next,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    value = state.lastName,
                    onValueChange = { viewModel.onAction(RegisterAction.LastNameChanged(it)) },
                    label = "Last Name (optional)",
                    imeAction = ImeAction.Next,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordTextField(
                    value = state.password,
                    onValueChange = { viewModel.onAction(RegisterAction.PasswordChanged(it)) },
                    label = "Password",
                    error = state.passwordError,
                    imeAction = ImeAction.Next,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordTextField(
                    value = state.confirmPassword,
                    onValueChange = { viewModel.onAction(RegisterAction.ConfirmPasswordChanged(it)) },
                    label = "Confirm Password",
                    error = state.confirmPasswordError,
                    imeAction = ImeAction.Done,
                    onImeAction = { viewModel.onAction(RegisterAction.RegisterClicked) },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.onAction(RegisterAction.RegisterClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = state.isRegisterEnabled
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Create Account")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { viewModel.onAction(RegisterAction.LoginClicked) }
                ) {
                    Text("Already have an account? Login")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
