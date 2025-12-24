package io.diasjakupov.dockify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.diasjakupov.dockify.features.auth.domain.repository.AuthRepository
import io.diasjakupov.dockify.features.auth.presentation.login.LoginScreen
import io.diasjakupov.dockify.features.auth.presentation.register.RegisterScreen
import io.diasjakupov.dockify.features.health.presentation.HealthScreen
import io.diasjakupov.dockify.features.location.presentation.nearby.NearbyScreen
import io.diasjakupov.dockify.ui.navigation.AppNavigator
import io.diasjakupov.dockify.ui.navigation.ForgotPasswordRoute
import io.diasjakupov.dockify.ui.navigation.HealthDashboardRoute
import io.diasjakupov.dockify.ui.navigation.HealthDetailRoute
import io.diasjakupov.dockify.ui.navigation.HomeRoute
import io.diasjakupov.dockify.ui.navigation.LoginRoute
import io.diasjakupov.dockify.ui.navigation.MapRoute
import io.diasjakupov.dockify.ui.navigation.NearbyRoute
import io.diasjakupov.dockify.ui.navigation.ProfileRoute
import io.diasjakupov.dockify.ui.navigation.RegisterRoute
import io.diasjakupov.dockify.ui.navigation.SettingsRoute
import io.diasjakupov.dockify.ui.navigation.rememberAppNavigator
import io.diasjakupov.dockify.ui.navigation.navSavedStateConfig
import io.diasjakupov.dockify.ui.navigation.MainScaffoldScreen
import io.diasjakupov.dockify.ui.navigation.PlaceholderScreen
import io.diasjakupov.dockify.ui.navigation.PlaceholderContent
import io.diasjakupov.dockify.ui.theme.DockifyTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

/**
 * CompositionLocal to provide AppNavigator throughout the app.
 */
val LocalAppNavigator = compositionLocalOf<AppNavigator> {
    error("No AppNavigator provided")
}

@Composable
@Preview
fun App() {
    DockifyTheme {
        val authRepository: AuthRepository = koinInject()

        // Determine initial route based on auth state
        var initialRoute by remember { mutableStateOf<NavKey?>(null) }

        LaunchedEffect(Unit) {
            initialRoute = if (authRepository.isAuthenticated()) {
                HealthDashboardRoute
            } else {
                LoginRoute
            }
        }

        // Wait for auth check to complete
        if (initialRoute == null) {
            return@DockifyTheme
        }

        val backStack = rememberNavBackStack(navSavedStateConfig, initialRoute!!)
        val navigator = rememberAppNavigator(backStack)

        CompositionLocalProvider(LocalAppNavigator provides navigator) {
            NavDisplay(
                backStack = backStack,
                onBack = { navigator.navigateBack() },
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                entryProvider = { key ->
                    when (key) {
                        // ============================================
                        // Auth Flow
                        // ============================================
                        is LoginRoute -> NavEntry(key) {
                            LoginScreen(
                                onNavigateToHome = { navigator.navigateToHomeAfterLogin() },
                                onNavigateToRegister = { navigator.navigateToRegister() },
                                onNavigateToForgotPassword = { navigator.navigateToForgotPassword() }
                            )
                        }

                        is RegisterRoute -> NavEntry(key) {
                            RegisterScreen(
                                onNavigateToLogin = { navigator.navigateToLogin() },
                                onNavigateBack = { navigator.navigateBack() }
                            )
                        }

                        is ForgotPasswordRoute -> NavEntry(key) {
                            // TODO: Implement ForgotPasswordScreen
                            PlaceholderScreen(
                                title = "Forgot Password",
                                onBack = { navigator.navigateBack() }
                            )
                        }

                        // ============================================
                        // Main Flow (Bottom Navigation)
                        // ============================================
                        is HomeRoute -> NavEntry(key) {
                            // TODO: Implement HomeScreen
                            MainScaffoldScreen(
                                currentRoute = key,
                                navigator = navigator
                            ) {
                                PlaceholderContent(title = "Home")
                            }
                        }

                        is HealthDashboardRoute -> NavEntry(key) {
                            MainScaffoldScreen(
                                currentRoute = key,
                                navigator = navigator
                            ) {
                                HealthScreen()
                            }
                        }

                        is HealthDetailRoute -> NavEntry(key) {
                            // TODO: Implement HealthDetailScreen
                            PlaceholderScreen(
                                title = "Health Detail: ${key.metricType}",
                                onBack = { navigator.navigateBack() }
                            )
                        }

                        is MapRoute -> NavEntry(key) {
                            MainScaffoldScreen(
                                currentRoute = key,
                                navigator = navigator
                            ) {
                                NearbyScreen()
                            }
                        }

                        is NearbyRoute -> NavEntry(key) {
                            // TODO: Implement NearbyScreen
                            PlaceholderScreen(
                                title = "Nearby Users",
                                onBack = { navigator.navigateBack() }
                            )
                        }

                        is ProfileRoute -> NavEntry(key) {
                            MainScaffoldScreen(
                                currentRoute = key,
                                navigator = navigator
                            ) {
                                PlaceholderContent(title = "Profile")
                            }
                        }

                        is SettingsRoute -> NavEntry(key) {
                            // TODO: Implement SettingsScreen
                            PlaceholderScreen(
                                title = "Settings",
                                onBack = { navigator.navigateBack() }
                            )
                        }

                        else -> error("Unknown route: $key")
                    }
                }
            )
        }
    }
}
