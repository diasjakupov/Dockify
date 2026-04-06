package io.diasjakupov.dockify.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Navigator helper class that centralizes navigation logic for the app.
 * Provides type-safe navigation methods using Navigation 3 NavKey routes.
 */
@Stable
class AppNavigator(
    private val backStack: NavBackStack<NavKey>
) {
    fun navigateTo(route: NavKey) {
        backStack.add(route)
    }

    fun navigateBack(): Boolean {
        return backStack.removeLastOrNull() != null
    }

    fun navigateToRoot(route: NavKey) {
        backStack.clear()
        backStack.add(route)
    }

    fun popUpTo(route: NavKey, inclusive: Boolean = false) {
        val index = backStack.indexOfLast { it == route }
        if (index >= 0) {
            val removeFrom = if (inclusive) index else index + 1
            while (backStack.size > removeFrom) {
                backStack.removeLast()
            }
        }
    }

    fun currentRoute(): NavKey? = backStack.lastOrNull()

    fun hasRoute(route: NavKey): Boolean = backStack.contains(route)

    // ============================================
    // Auth Navigation
    // ============================================

    fun navigateToLogin() {
        navigateToRoot(LoginRoute)
    }

    fun navigateToRegister() {
        navigateTo(RegisterRoute)
    }

    /** Navigate to Health tab after successful login, clearing auth screens. */
    fun navigateToHealthAfterLogin() {
        navigateToRoot(HealthRoute)
    }

    // ============================================
    // Main Flow Navigation
    // ============================================

    fun navigateToHealth() {
        navigateToRoot(HealthRoute)
    }

    fun navigateToNearby() {
        navigateToRoot(NearbyRoute)
    }

    fun navigateToDocuments() {
        navigateToRoot(DocumentsRoute)
    }

    fun navigateToChat(docId: String? = null, documentName: String? = null) {
        if (docId == null) {
            navigateToRoot(ChatRoute())
        } else {
            navigateTo(ChatRoute(docId = docId, documentName = documentName))
        }
    }

    fun navigateToProfile() {
        navigateTo(ProfileRoute)
    }

    fun navigateToTopLevel(destination: TopLevelDestination) {
        navigateToRoot(destination.route)
    }
}

@Composable
fun rememberAppNavigator(backStack: NavBackStack<NavKey>): AppNavigator {
    return remember(backStack) {
        AppNavigator(backStack)
    }
}
