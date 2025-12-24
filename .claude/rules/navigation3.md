# Navigation 3 Implementation Guide (Compose Multiplatform)

This document defines rules and patterns for implementing navigation using Jetpack Navigation 3 in this Kotlin Multiplatform project.

---

## Core Dependencies

```toml
# gradle/libs.versions.toml
[versions]
composeMultiplatform = "1.10.0-beta01"  # Minimum version for Nav3 support
navigation3-ui = "1.1.0-alpha01"
lifecycle-viewmodel-navigation3 = "2.10.0-alpha07"

[libraries]
navigation3-ui = { module = "org.jetbrains.androidx.navigation3:navigation3-ui", version.ref = "navigation3-ui" }
lifecycle-viewmodel-navigation3 = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3", version.ref = "lifecycle-viewmodel-navigation3" }
```

```kotlin
// build.gradle.kts (commonMain dependencies)
commonMain.dependencies {
    implementation(libs.navigation3.ui)
    implementation(libs.lifecycle.viewmodel.navigation3)
}
```

---

## Route Definition Rules

### Rule 1: Routes Must Implement NavKey
All navigation destinations must be defined as `@Serializable` classes/objects implementing `NavKey`.

```kotlin
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Simple route without parameters
@Serializable
data object HomeRoute : NavKey

// Route with parameters
@Serializable
data class DetailRoute(val itemId: String) : NavKey

// Route with multiple parameters
@Serializable
data class ProfileRoute(
    val userId: String,
    val tab: String = "overview"  // Default values allowed
) : NavKey
```

### Rule 2: SavedStateConfiguration for Serialization
Create a `SavedStateConfiguration` with polymorphic serializers for all routes.

```kotlin
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(HomeRoute::class, HomeRoute.serializer())
            subclass(DetailRoute::class, DetailRoute.serializer())
            subclass(ProfileRoute::class, ProfileRoute.serializer())
            // Register all routes here
        }
    }
}
```

### Rule 3: Route Naming Convention
- Use `Route` suffix for all navigation keys: `HomeRoute`, `SettingsRoute`
- Use descriptive names that reflect the destination purpose
- Avoid abbreviations

---

## Back Stack Management

### Rule 4: Use rememberNavBackStack with SavedStateConfiguration
Always use `rememberNavBackStack(config, initialRoute)` to ensure state survives configuration changes.

```kotlin
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(navSavedStateConfig, HomeRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { /* ... */ }
    )
}
```

### Rule 5: Navigator Helper Class
Create a centralized navigator class for type-safe navigation.

```kotlin
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

@Stable
class AppNavigator(private val backStack: NavBackStack<NavKey>) {

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
}

@Composable
fun rememberAppNavigator(backStack: NavBackStack<NavKey>): AppNavigator {
    return remember(backStack) { AppNavigator(backStack) }
}
```

---

## NavDisplay Implementation

### Rule 6: Standard NavDisplay Structure

```kotlin
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(navSavedStateConfig, HomeRoute)
    val navigator = rememberAppNavigator(backStack)

    NavDisplay(
        backStack = backStack,
        onBack = { navigator.navigateBack() },
        entryProvider = { key ->
            when (key) {
                is HomeRoute -> NavEntry(key) {
                    HomeScreen(
                        onNavigateToDetail = { id ->
                            navigator.navigateTo(DetailRoute(id))
                        }
                    )
                }
                is DetailRoute -> NavEntry(key) {
                    DetailScreen(itemId = key.itemId)
                }
                else -> error("Unknown route: $key")
            }
        }
    )
}
```

### Rule 7: Provide Navigator via CompositionLocal

```kotlin
val LocalAppNavigator = compositionLocalOf<AppNavigator> {
    error("No AppNavigator provided")
}

@Composable
fun App() {
    val backStack = rememberNavBackStack(navSavedStateConfig, LoginRoute)
    val navigator = rememberAppNavigator(backStack)

    CompositionLocalProvider(LocalAppNavigator provides navigator) {
        NavDisplay(
            backStack = backStack,
            onBack = { navigator.navigateBack() },
            entryProvider = { /* ... */ }
        )
    }
}

// Usage in screens
@Composable
fun SomeScreen() {
    val navigator = LocalAppNavigator.current
    Button(onClick = { navigator.navigateTo(DetailRoute("123")) }) {
        Text("Navigate")
    }
}
```

---

## Bottom Navigation with Navigation 3

### Rule 8: Bottom Navigation Items

```kotlin
enum class BottomNavItem(
    val route: NavKey,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(HomeRoute, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    HEALTH(HealthRoute, "Health", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    PROFILE(ProfileRoute, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun Nav3BottomNavigation(
    currentRoute: NavKey,
    onNavigate: (BottomNavItem) -> Unit
) {
    NavigationBar {
        BottomNavItem.entries.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}
```

### Rule 9: Main Scaffold with Bottom Navigation

```kotlin
@Composable
fun MainScaffoldScreen(
    currentRoute: NavKey,
    navigator: AppNavigator,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            Nav3BottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { item ->
                    navigator.navigateToRoot(item.route)
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            content()
        }
    }
}
```

---

## Scene Strategies

### Rule 10: Bottom Sheet Navigation

```kotlin
@Serializable
data class FilterRoute(val category: String) : NavKey

NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    sceneStrategy = BottomSheetSceneStrategy,
    entryProvider = { key ->
        when (key) {
            is FilterRoute -> NavEntry(
                key = key,
                metadata = bottomSheet()
            ) {
                Surface(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) {
                    FilterSheet(category = key.category)
                }
            }
            // ...
        }
    }
)
```

### Rule 11: Dialog Navigation

```kotlin
@Serializable
data class ConfirmDeleteRoute(val itemId: String) : NavKey

NavDisplay(
    sceneStrategy = DialogSceneStrategy,
    entryProvider = { key ->
        when (key) {
            is ConfirmDeleteRoute -> NavEntry(
                key = key,
                metadata = dialog()
            ) {
                ConfirmDeleteDialog(
                    itemId = key.itemId,
                    onConfirm = { /* delete */ },
                    onDismiss = { backStack.removeLastOrNull() }
                )
            }
            // ...
        }
    }
)
```

---

## iOS-Specific Configuration

### Rule 12: Edge Pan Gesture (iOS)
On iOS, configure end edge pan gestures using `EndEdgePanGestureBehavior`:

```kotlin
// iOS-specific: End edge is right in LTR, left in RTL
// Start edge is always bound to back gesture
NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    // Configure as needed for iOS edge gestures
    entryProvider = { /* ... */ }
)
```

---

## Complete Example: App.kt

```kotlin
package io.diasjakupov.dockify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay

val LocalAppNavigator = compositionLocalOf<AppNavigator> {
    error("No AppNavigator provided")
}

@Composable
fun App() {
    DockifyTheme {
        val backStack = rememberNavBackStack(navSavedStateConfig, LoginRoute)
        val navigator = rememberAppNavigator(backStack)

        CompositionLocalProvider(LocalAppNavigator provides navigator) {
            NavDisplay(
                backStack = backStack,
                onBack = { navigator.navigateBack() },
                entryProvider = { key ->
                    when (key) {
                        is LoginRoute -> NavEntry(key) {
                            LoginScreen(
                                onNavigateToHome = { navigator.navigateToHomeAfterLogin() },
                                onNavigateToRegister = { navigator.navigateToRegister() }
                            )
                        }
                        is RegisterRoute -> NavEntry(key) {
                            RegisterScreen(
                                onNavigateToLogin = { navigator.navigateToLogin() },
                                onNavigateBack = { navigator.navigateBack() }
                            )
                        }
                        is HomeRoute -> NavEntry(key) {
                            MainScaffoldScreen(currentRoute = key, navigator = navigator) {
                                HomeScreen()
                            }
                        }
                        // ... other routes
                        else -> error("Unknown route: $key")
                    }
                }
            )
        }
    }
}
```

---

## File Structure Convention

```
ui/navigation/
├── Routes.kt              # All route definitions + SavedStateConfiguration
├── AppNavigator.kt        # Navigator helper class
└── MainScaffold.kt        # Bottom navigation scaffold

features/{feature}/
├── presentation/
│   ├── {Feature}Screen.kt
│   └── {Feature}ViewModel.kt
└── ...
```

---

## Common Mistakes to Avoid

1. **Don't forget SavedStateConfiguration** - Required for `rememberNavBackStack` to persist state
2. **Don't skip polymorphic registration** - All routes must be registered in `SerializersModule`
3. **Don't use type parameters on rememberNavBackStack** - It infers the type automatically
4. **Don't hardcode route strings** - Use type-safe NavKey objects
5. **Don't navigate in Composable scope** - Use callbacks or LaunchedEffect
6. **Don't forget the else branch** - Always handle unknown routes in entryProvider
7. **Don't pass complex objects as route parameters** - Keep routes serializable with primitive types
8. **Don't modify back stack from multiple places** - Centralize navigation logic in a Navigator class

---

## Quick Reference

| Operation | Code |
|-----------|------|
| Initialize backStack | `rememberNavBackStack(config, InitialRoute)` |
| Navigate forward | `navigator.navigateTo(DestinationRoute)` |
| Navigate back | `navigator.navigateBack()` |
| Navigate to root | `navigator.navigateToRoot(route)` |
| Check current route | `navigator.currentRoute()` |
| Pop to specific route | `navigator.popUpTo(route, inclusive)` |

---

## Required Imports

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
```