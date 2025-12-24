package io.diasjakupov.dockify.ui.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * Navigation routes for the Dockify app using Navigation 3.
 * All routes implement NavKey and are @Serializable for type-safe navigation.
 */

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
// Main Flow Routes (Bottom Navigation)
// ============================================

@Serializable
data object HomeRoute : NavKey

@Serializable
data object HealthDashboardRoute : NavKey

@Serializable
data class HealthDetailRoute(val metricType: String) : NavKey

@Serializable
data object MapRoute : NavKey

@Serializable
data object NearbyRoute : NavKey

@Serializable
data object ProfileRoute : NavKey

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
            subclass(HomeRoute::class, HomeRoute.serializer())
            subclass(HealthDashboardRoute::class, HealthDashboardRoute.serializer())
            subclass(HealthDetailRoute::class, HealthDetailRoute.serializer())
            subclass(MapRoute::class, MapRoute.serializer())
            subclass(NearbyRoute::class, NearbyRoute.serializer())
            subclass(ProfileRoute::class, ProfileRoute.serializer())
            subclass(SettingsRoute::class, SettingsRoute.serializer())
        }
    }
}

/**
 * Top-level destinations for bottom navigation.
 */
enum class TopLevelDestination(val route: NavKey) {
    HOME(HomeRoute),
    HEALTH(HealthDashboardRoute),
    MAP(MapRoute),
    PROFILE(ProfileRoute)
}
