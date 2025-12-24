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
    /**
     * Navigate to a new destination by adding it to the back stack.
     */
    fun navigateTo(route: NavKey) {
        backStack.add(route)
    }

    /**
     * Navigate back by removing the last entry from the back stack.
     * @return true if navigation occurred, false if back stack is empty
     */
    fun navigateBack(): Boolean {
        return backStack.removeLastOrNull() != null
    }

    /**
     * Clear the back stack and navigate to a new root destination.
     */
    fun navigateToRoot(route: NavKey) {
        backStack.clear()
        backStack.add(route)
    }

    /**
     * Pop up to a specific route in the back stack.
     * @param route The route to pop up to
     * @param inclusive If true, also removes the target route
     */
    fun popUpTo(route: NavKey, inclusive: Boolean = false) {
        val index = backStack.indexOfLast { it == route }
        if (index >= 0) {
            val removeFrom = if (inclusive) index else index + 1
            while (backStack.size > removeFrom) {
                backStack.removeLast()
            }
        }
    }

    /**
     * Get the current route (top of the back stack).
     */
    fun currentRoute(): NavKey? = backStack.lastOrNull()

    /**
     * Check if a specific route is in the back stack.
     */
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

    fun navigateToForgotPassword() {
        navigateTo(ForgotPasswordRoute)
    }

    /**
     * Navigate to home after successful login, clearing auth screens.
     */
    fun navigateToHomeAfterLogin() {
        navigateToRoot(HomeRoute)
    }

    // ============================================
    // Main Flow Navigation
    // ============================================

    fun navigateToHome() {
        navigateTo(HomeRoute)
    }

    fun navigateToHealth() {
        navigateTo(HealthDashboardRoute)
    }

    fun navigateToHealthDetail(metricType: String) {
        navigateTo(HealthDetailRoute(metricType))
    }

    fun navigateToMap() {
        navigateTo(MapRoute)
    }

    fun navigateToNearby() {
        navigateTo(NearbyRoute)
    }

    fun navigateToProfile() {
        navigateTo(ProfileRoute)
    }

    fun navigateToSettings() {
        navigateTo(SettingsRoute)
    }

    // ============================================
    // Top-Level Navigation (Bottom Bar)
    // ============================================

    /**
     * Navigate to a top-level destination.
     * This replaces the current back stack with the new destination.
     */
    fun navigateToTopLevel(destination: TopLevelDestination) {
        navigateToRoot(destination.route)
    }
}

/**
 * Remember an AppNavigator instance scoped to the composition.
 */
@Composable
fun rememberAppNavigator(backStack: NavBackStack<NavKey>): AppNavigator {
    return remember(backStack) {
        AppNavigator(backStack)
    }
}
