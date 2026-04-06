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
import io.diasjakupov.dockify.features.auth.presentation.profile.ProfileScreen
import io.diasjakupov.dockify.features.chat.presentation.ChatScreen
import io.diasjakupov.dockify.features.documents.presentation.documents.DocumentsScreen
import io.diasjakupov.dockify.features.location.presentation.nearby.NearbyScreen
import io.diasjakupov.dockify.ui.navigation.AppNavigator
import io.diasjakupov.dockify.ui.navigation.HealthRoute
import io.diasjakupov.dockify.ui.navigation.LoginRoute
import io.diasjakupov.dockify.ui.navigation.MainScaffoldScreen
import io.diasjakupov.dockify.ui.navigation.ChatRoute
import io.diasjakupov.dockify.ui.navigation.DocumentsRoute
import io.diasjakupov.dockify.ui.navigation.NearbyRoute
import io.diasjakupov.dockify.ui.navigation.ProfileRoute
import io.diasjakupov.dockify.ui.navigation.RegisterRoute
import io.diasjakupov.dockify.ui.navigation.navSavedStateConfig
import io.diasjakupov.dockify.ui.navigation.rememberAppNavigator
import io.diasjakupov.dockify.ui.theme.DockifyTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

val LocalAppNavigator = compositionLocalOf<AppNavigator> {
    error("No AppNavigator provided")
}

@Composable
@Preview
fun App() {
    DockifyTheme {
        val authRepository: AuthRepository = koinInject()

        var initialRoute by remember { mutableStateOf<NavKey?>(null) }

        LaunchedEffect(Unit) {
            initialRoute = if (authRepository.isAuthenticated()) {
                HealthRoute
            } else {
                LoginRoute
            }
        }

        if (initialRoute == null) return@DockifyTheme

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
                        // Auth Flow
                        is LoginRoute -> NavEntry(key) {
                            LoginScreen(
                                onNavigateToHome = { navigator.navigateToHealthAfterLogin() },
                                onNavigateToRegister = { navigator.navigateToRegister() }
                            )
                        }
                        is RegisterRoute -> NavEntry(key) {
                            RegisterScreen(
                                onNavigateToLogin = { navigator.navigateToLogin() },
                                onNavigateBack = { navigator.navigateBack() }
                            )
                        }
                        // Tab 1: Health
                        is HealthRoute -> NavEntry(key) {
                            MainScaffoldScreen(currentRoute = key, navigator = navigator) {
                                HealthScreen()
                            }
                        }

                        // Tab 2: Nearby
                        is NearbyRoute -> NavEntry(key) {
                            MainScaffoldScreen(currentRoute = key, navigator = navigator) {
                                NearbyScreen(
                                    onNavigateToProfile = { navigator.navigateToProfile() },
                                    onOpenGpsSettings = { }
                                )
                            }
                        }

                        // Tab 3: Documents
                        is DocumentsRoute -> NavEntry(key) {
                            MainScaffoldScreen(currentRoute = key, navigator = navigator) {
                                DocumentsScreen()
                            }
                        }

                        // Tab 4: Chat
                        is ChatRoute -> {
                            if (key.docId == null) {
                                NavEntry(key) {
                                    MainScaffoldScreen(currentRoute = key, navigator = navigator) {
                                        ChatScreen()
                                    }
                                }
                            } else {
                                NavEntry(key) {
                                    ChatScreen(
                                        docId = key.docId,
                                        documentName = key.documentName,
                                        onBack = { navigator.navigateBack() }
                                    )
                                }
                            }
                        }

                        // Pushed screens
                        is ProfileRoute -> NavEntry(key) {
                            ProfileScreen(
                                onNavigateBack = { navigator.navigateBack() },
                                onNavigateToLogin = { navigator.navigateToLogin() }
                            )
                        }
                        else -> error("Unknown route: $key")
                    }
                }
            )
        }
    }
}
