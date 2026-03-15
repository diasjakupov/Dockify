package io.diasjakupov.dockify.ui.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

// ============================================
// Auth Flow Routes
// ============================================

@Serializable
data object LoginRoute : NavKey

@Serializable
data object RegisterRoute : NavKey

@Serializable
data object ForgotPasswordRoute : NavKey

// ============================================
// Main Flow Routes (Bottom Navigation — 3 tabs)
// ============================================

/** Tab 1: Combined Health/Home dashboard */
@Serializable
data object HealthRoute : NavKey

/** Drill-down from a vital card */
@Serializable
data class HealthDetailRoute(val metricType: String) : NavKey

/** Tab 2: Nearby users screen */
@Serializable
data object NearbyRoute : NavKey

/** Tab 3: Documents screen */
@Serializable
data object DocumentsRoute : NavKey

/** Pushed from Nearby top bar profile icon */
@Serializable
data object ProfileRoute : NavKey

/** Pushed from Health top bar settings icon */
@Serializable
data object SettingsRoute : NavKey

/**
 * SavedStateConfiguration for Navigation 3 back stack serialization.
 * Registers all routes for polymorphic serialization.
 */
val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(LoginRoute::class, LoginRoute.serializer())
            subclass(RegisterRoute::class, RegisterRoute.serializer())
            subclass(ForgotPasswordRoute::class, ForgotPasswordRoute.serializer())
            subclass(HealthRoute::class, HealthRoute.serializer())
            subclass(HealthDetailRoute::class, HealthDetailRoute.serializer())
            subclass(NearbyRoute::class, NearbyRoute.serializer())
            subclass(DocumentsRoute::class, DocumentsRoute.serializer())
            subclass(ProfileRoute::class, ProfileRoute.serializer())
            subclass(SettingsRoute::class, SettingsRoute.serializer())
        }
    }
}

/**
 * The two top-level bottom-navigation destinations.
 */
enum class TopLevelDestination(val route: NavKey) {
    HEALTH(HealthRoute),
    NEARBY(NearbyRoute),
    DOCUMENTS(DocumentsRoute)
}
