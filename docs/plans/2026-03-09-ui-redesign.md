# UI Redesign Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Redesign Dockify's UI with a clean minimal light (Notion-inspired) design system, collapse 4 bottom nav tabs to 2, and refocus the Health screen on medical data.

**Architecture:** Design system tokens updated first (Color → Theme → Dimensions → Type), then navigation restructured (Routes → AppNavigator → MainScaffold → App), then screens redesigned top-down (Health → Nearby → Profile).

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Material3, Navigation 3, Koin

---

## Task 1: Update Color.kt — Add Notion-Style Semantic Tokens

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/theme/Color.kt`

**Step 1: Add the new semantic color tokens at the bottom of the file**

Append after the `HealthStatusColors` object:

```kotlin
/**
 * Notion-inspired semantic color tokens for the redesigned UI.
 */
object NotionColors {
    // Backgrounds
    val BackgroundWarm = Color(0xFFFAFAF9)      // Screen backgrounds
    val SurfaceWhite = Color(0xFFFFFFFF)         // Cards
    val SurfaceSecondary = Color(0xFFF5F4F2)     // Input fields, avatar backgrounds

    // Text
    val TextPrimary = Color(0xFF2D2D2D)          // Main text
    val TextSecondary = Color(0xFF6B7280)        // Labels, icons
    val TextTertiary = Color(0xFF9CA3AF)         // Captions, units, timestamps

    // Interactive
    val Accent = Color(0xFF4F7FE8)               // Active states, links, progress
    val AccentLight = Color(0xFFEEF3FD)          // Accent backgrounds

    // Borders
    val Divider = Color(0xFFE8E7E4)              // Card borders, separators

    // Status (muted to fit warm palette)
    val StatusSuccess = Color(0xFF22C55E)        // Good/excellent
    val StatusWarning = Color(0xFFF59E0B)        // Warning
    val StatusError = Color(0xFFEF4444)          // Critical/error
}
```

**Step 2: Build to verify no compile errors**

```bash
./gradlew :composeApp:assembleDebug 2>&1 | tail -20
```
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/theme/Color.kt
git commit -m "feat: add Notion-style semantic color tokens"
```

---

## Task 2: Update Theme.kt — Apply New Colors to Light Scheme

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/theme/Theme.kt`

**Step 1: Replace the `LightColorScheme` definition**

Replace the existing `private val LightColorScheme = lightColorScheme(...)` block with:

```kotlin
private val LightColorScheme = lightColorScheme(
    primary = NotionColors.TextPrimary,
    onPrimary = Color.White,
    primaryContainer = NotionColors.SurfaceSecondary,
    onPrimaryContainer = NotionColors.TextPrimary,

    secondary = NotionColors.TextSecondary,
    onSecondary = Color.White,
    secondaryContainer = NotionColors.SurfaceSecondary,
    onSecondaryContainer = NotionColors.TextPrimary,

    tertiary = NotionColors.Accent,
    onTertiary = Color.White,
    tertiaryContainer = NotionColors.AccentLight,
    onTertiaryContainer = NotionColors.Accent,

    error = NotionColors.StatusError,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),

    background = NotionColors.BackgroundWarm,
    onBackground = NotionColors.TextPrimary,
    surface = NotionColors.SurfaceWhite,
    onSurface = NotionColors.TextPrimary,
    surfaceVariant = NotionColors.SurfaceSecondary,
    onSurfaceVariant = NotionColors.TextSecondary,

    outline = NotionColors.Divider,
    outlineVariant = NotionColors.Divider,
    scrim = NotionColors.TextPrimary
)
```

**Step 2: Build to verify**

```bash
./gradlew :composeApp:assembleDebug 2>&1 | tail -20
```
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/theme/Theme.kt
git commit -m "feat: apply Notion light color scheme to Material3 tokens"
```

---

## Task 3: Update Dimensions.kt — Notion Card Geometry

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/theme/Dimensions.kt`

**Step 1: Update card dimensions and add new tokens**

Replace the card-related fields in the `Dimensions` data class:

```kotlin
// Card dimensions
val cardPadding: Dp = 16.dp,
val cardElevation: Dp = 0.dp,          // was 2.dp — Notion uses border, not shadow
val cardCornerRadius: Dp = 12.dp,      // was 16.dp
val cardBorderWidth: Dp = 1.dp,        // new — Notion-style 1px border

// Health metric card dimensions
val metricCardHeight: Dp = 110.dp,     // was 120.dp — slightly more compact
val metricCardMinWidth: Dp = 160.dp,
val metricIconSize: Dp = 20.dp,        // was 48.dp — icon in card is small

// Profile avatar
val avatarSizeLarge: Dp = 64.dp,       // new
val avatarSizeMedium: Dp = 36.dp,      // new — top bar avatar
val statusDotSize: Dp = 8.dp,          // new — health status dot
```

**Step 2: Build to verify**

```bash
./gradlew :composeApp:assembleDebug 2>&1 | tail -20
```
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/theme/Dimensions.kt
git commit -m "feat: update card geometry for Notion design system"
```

---

## Task 4: Update Type.kt — Section Header + Metric Styles

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/theme/Type.kt`

**Step 1: Update `HealthTextStyles` and add `sectionHeader`**

Replace the entire `HealthTextStyles` object and append a new `DockifyTextStyles` object:

```kotlin
/**
 * Health-specific text styles for metric displays.
 */
object HealthTextStyles {
    val metricValue = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 52.sp,
        lineHeight = 60.sp,
        letterSpacing = (-1).sp
    )

    val metricValueSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    )

    val metricUnit = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.25.sp
    )

    val chartLabel = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp
    )
}

/**
 * Shared text styles for the redesigned Notion-style UI.
 */
object DockifyTextStyles {
    /**
     * Notion-style section header: ALL CAPS, letter-spaced, muted.
     * Usage: Text("VITALS", style = DockifyTextStyles.sectionHeader)
     */
    val sectionHeader = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.2.sp
    )
}
```

**Step 2: Build to verify**

```bash
./gradlew :composeApp:assembleDebug 2>&1 | tail -20
```
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/theme/Type.kt
git commit -m "feat: update typography — section headers and metric styles"
```

---

## Task 5: Update Routes.kt — 2-Tab Navigation Structure

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/navigation/Routes.kt`

**Step 1: Replace the entire file contents**

```kotlin
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
// Main Flow Routes (Bottom Navigation — 2 tabs)
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
    NEARBY(NearbyRoute)
}
```

**Step 2: Build — expect errors in AppNavigator.kt and App.kt referencing old routes. That is fine; we fix them in Tasks 6–8.**

```bash
./gradlew :composeApp:assembleDebug 2>&1 | grep "error:" | head -30
```

**Step 3: Commit (even with errors — intermediate state)**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/navigation/Routes.kt
git commit -m "feat: collapse to 2-tab route structure (HealthRoute + NearbyRoute)"
```

---

## Task 6: Update AppNavigator.kt — Remove Stale Routes, Add New Methods

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/navigation/AppNavigator.kt`

**Step 1: Replace the entire file contents**

```kotlin
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

    fun navigateToForgotPassword() {
        navigateTo(ForgotPasswordRoute)
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

    fun navigateToHealthDetail(metricType: String) {
        navigateTo(HealthDetailRoute(metricType))
    }

    fun navigateToNearby() {
        navigateToRoot(NearbyRoute)
    }

    fun navigateToProfile() {
        navigateTo(ProfileRoute)
    }

    fun navigateToSettings() {
        navigateTo(SettingsRoute)
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
```

**Step 2: Build — expect remaining errors only in App.kt now**

```bash
./gradlew :composeApp:assembleDebug 2>&1 | grep "error:" | head -20
```

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/navigation/AppNavigator.kt
git commit -m "feat: update AppNavigator for 2-tab route structure"
```

---

## Task 7: Update MainScaffold.kt — 2-Tab Bottom Navigation

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/navigation/MainScaffold.kt`

**Step 1: Replace the entire file contents**

```kotlin
package io.diasjakupov.dockify.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import io.diasjakupov.dockify.ui.components.common.DockifyScaffold
import io.diasjakupov.dockify.ui.components.common.TopBarConfig

/**
 * The two bottom navigation items.
 */
enum class BottomNavItem(
    val route: NavKey,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HEALTH(
        route = HealthRoute,
        label = "Health",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    ),
    NEARBY(
        route = NearbyRoute,
        label = "Nearby",
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.PeopleAlt
    )
}

/**
 * Main scaffold wrapping content with the 2-item bottom navigation bar.
 */
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
                onNavigate = { item -> navigator.navigateToRoot(item.route) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content()
        }
    }
}

/**
 * Bottom navigation bar with 2 items: Health and Nearby.
 */
@Composable
fun Nav3BottomNavigation(
    currentRoute: NavKey,
    onNavigate: (BottomNavItem) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = androidx.compose.ui.unit.Dp(0f)
    ) {
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

/**
 * Placeholder screen for unimplemented destinations (e.g. ForgotPassword, Settings).
 */
@Composable
fun PlaceholderScreen(
    title: String,
    onBack: () -> Unit
) {
    DockifyScaffold(
        topBarConfig = TopBarConfig.Simple(
            title = title,
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
```

**Step 2: Build — only App.kt errors should remain**

```bash
./gradlew :composeApp:assembleDebug 2>&1 | grep "error:" | head -20
```

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/ui/navigation/MainScaffold.kt
git commit -m "feat: simplify bottom nav to 2 tabs (Health + Nearby)"
```

---

## Task 8: Update App.kt — Wire Up New Routes

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/App.kt`

**Step 1: Replace the entire file contents**

```kotlin
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
import io.diasjakupov.dockify.ui.navigation.HealthDetailRoute
import io.diasjakupov.dockify.ui.navigation.HealthRoute
import io.diasjakupov.dockify.ui.navigation.LoginRoute
import io.diasjakupov.dockify.ui.navigation.MainScaffoldScreen
import io.diasjakupov.dockify.ui.navigation.NearbyRoute
import io.diasjakupov.dockify.ui.navigation.PlaceholderScreen
import io.diasjakupov.dockify.ui.navigation.ProfileRoute
import io.diasjakupov.dockify.ui.navigation.RegisterRoute
import io.diasjakupov.dockify.ui.navigation.SettingsRoute
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
                            PlaceholderScreen(
                                title = "Forgot Password",
                                onBack = { navigator.navigateBack() }
                            )
                        }

                        // Tab 1: Health
                        is HealthRoute -> NavEntry(key) {
                            MainScaffoldScreen(currentRoute = key, navigator = navigator) {
                                HealthScreen(
                                    onNavigateToDetail = { metricType ->
                                        navigator.navigateToHealthDetail(metricType)
                                    },
                                    onNavigateToSettings = { navigator.navigateToSettings() }
                                )
                            }
                        }
                        is HealthDetailRoute -> NavEntry(key) {
                            PlaceholderScreen(
                                title = key.metricType,
                                onBack = { navigator.navigateBack() }
                            )
                        }

                        // Tab 2: Nearby
                        is NearbyRoute -> NavEntry(key) {
                            MainScaffoldScreen(currentRoute = key, navigator = navigator) {
                                NearbyScreen(
                                    onNavigateToProfile = { navigator.navigateToProfile() }
                                )
                            }
                        }

                        // Pushed screens
                        is ProfileRoute -> NavEntry(key) {
                            PlaceholderScreen(
                                title = "Profile",
                                onBack = { navigator.navigateBack() }
                            )
                        }
                        is SettingsRoute -> NavEntry(key) {
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
```

**Step 2: Build — should compile cleanly now**

```bash
./gradlew :composeApp:assembleDebug 2>&1 | tail -20
```
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/App.kt
git commit -m "feat: wire up 2-tab navigation in App.kt"
```

---

## Task 9: Add Navigation Callbacks to HealthScreen

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/health/presentation/HealthScreen.kt`

**Context:** Currently `HealthScreen()` takes no parameters. We need to add `onNavigateToDetail` and `onNavigateToSettings` callbacks so App.kt can pass them.

**Step 1: Add the two callback parameters to `HealthScreen`**

Find the function signature:
```kotlin
fun HealthScreen(
    viewModel: HealthViewModel = koinViewModel(),
```

Replace with:
```kotlin
fun HealthScreen(
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: HealthViewModel = koinViewModel(),
```

**Step 2: Wire the settings callback to the top bar**

Inside `HealthScreen`, find where the top bar settings icon is rendered (in the custom top bar section). Pass `onNavigateToSettings` to that icon's `onClick`. The exact location will vary — search for the `Settings` icon reference inside the top bar composable and replace its `onClick = {}` with `onClick = onNavigateToSettings`.

**Step 3: Wire the detail callback to vital cards**

Find `VitalCard` click handlers in `HealthVitalsGrid.kt`. The `HealthVitalsSection` or the loop calling `VitalCard` should invoke `onNavigateToDetail(vitalSign.type.name)` on tap. Pass `onNavigateToDetail` down from `HealthScreen` → `HealthVitalsSection` → each card's `onClick`.

**Step 4: Build to verify**

```bash
./gradlew :composeApp:assembleDebug 2>&1 | tail -20
```
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/health/presentation/HealthScreen.kt
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/health/presentation/components/HealthVitalsGrid.kt
git commit -m "feat: add navigation callbacks to HealthScreen"
```

---

## Task 10: Redesign HealthScreen Layout — Medical Focus

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/health/presentation/HealthScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/health/presentation/components/HealthVitalsGrid.kt`
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/health/presentation/components/StatusOverviewCard.kt`

### Step 1: Create StatusOverviewCard.kt

This is the new hero card — a compact grid showing all vitals with status dots at a glance.

```kotlin
package io.diasjakupov.dockify.features.health.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.ui.theme.DockifyTextStyles
import io.diasjakupov.dockify.ui.theme.NotionColors

/**
 * Hero card at the top of the Health screen.
 * Shows all vitals as a compact status grid so users can see at a glance if anything is off.
 */
@Composable
fun StatusOverviewCard(
    vitals: List<VitalSign>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, NotionColors.Divider, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "STATUS OVERVIEW",
            style = DockifyTextStyles.sectionHeader,
            color = NotionColors.TextTertiary
        )

        vitals.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { vital ->
                    StatusOverviewItem(
                        vital = vital,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if odd number
                if (row.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatusOverviewItem(
    vital: VitalSign,
    modifier: Modifier = Modifier
) {
    val statusColor = when (vital.status) {
        VitalStatus.EXCELLENT, VitalStatus.GOOD -> NotionColors.StatusSuccess
        VitalStatus.NORMAL -> NotionColors.TextSecondary
        VitalStatus.WARNING -> NotionColors.StatusWarning
        VitalStatus.ALERT, VitalStatus.CRITICAL -> NotionColors.StatusError
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Column {
            Text(
                text = vital.label,
                style = MaterialTheme.typography.labelSmall,
                color = NotionColors.TextTertiary
            )
            Text(
                text = "${vital.value} ${vital.unit}",
                style = MaterialTheme.typography.labelMedium,
                color = NotionColors.TextPrimary
            )
        }
    }
}
```

### Step 2: Update HealthScreen LazyColumn layout

In `HealthScreen.kt`, find the `LazyColumn` content block inside the main content composable. Replace the content order to:

1. **Remove** `TodaySummaryCard` item entirely
2. **Remove** `ActivityRingCard` item entirely
3. **Add** `StatusOverviewCard` as the first item (passing `state.healthMetrics` mapped to `List<VitalSign>`)
4. **Keep** the section header + vitals grid (update header text to use `DockifyTextStyles.sectionHeader`)
5. **Keep** the AI recommendation card
6. **Add** an `ACTIVITY` section header below recommendation with steps/calories as simple stat rows
7. **Keep** sync status footer

The section headers pattern to use throughout:
```kotlin
item {
    Text(
        text = "VITALS",
        style = DockifyTextStyles.sectionHeader,
        color = NotionColors.TextTertiary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
```

### Step 3: Update VitalCard in HealthVitalsGrid.kt

Remove the colored card background. Update each `VitalCard` to use:
- White surface background with `1px` border in `NotionColors.Divider`
- `12dp` corner radius
- Status dot (small circle) instead of status chip/badge

```kotlin
Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(1.dp, NotionColors.Divider),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
) { ... }
```

### Step 4: Build

```bash
./gradlew :composeApp:assembleDebug 2>&1 | tail -20
```
Expected: BUILD SUCCESSFUL

### Step 5: Commit

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/health/presentation/
git commit -m "feat: redesign Health screen for medical-data focus"
```

---

## Task 11: Update NearbyScreen — Profile Avatar in Top Bar + Distance Cards

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/location/presentation/nearby/NearbyScreen.kt`

**Step 1: Add `onNavigateToProfile` parameter**

```kotlin
@Composable
fun NearbyScreen(
    onNavigateToProfile: () -> Unit = {},
    viewModel: NearbyViewModel = koinViewModel(),
    permissionHandler: LocationPermissionHandler = koinInject()
)
```

**Step 2: Update `NearbyTopBar` to accept and show profile avatar**

Replace the `NearbyTopBar` composable signature and body:

```kotlin
@Composable
private fun NearbyTopBar(
    isRefreshing: Boolean,
    canRefresh: Boolean,
    onRefresh: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Nearby",
            style = MaterialTheme.typography.titleLarge,
            color = NotionColors.TextPrimary,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onRefresh,
                enabled = canRefresh
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh",
                        tint = NotionColors.TextSecondary)
                }
            }
            // Profile avatar button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NotionColors.SurfaceSecondary)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(20.dp),
                    tint = NotionColors.TextSecondary
                )
            }
        }
    }
}
```

Pass `onProfileClick = onNavigateToProfile` where `NearbyTopBar` is called.

**Step 3: Update `NearbyUserCard` — show distance instead of raw coordinates**

The `NearbyUser` domain model has `location.latitude` and `location.longitude`. For now, display a formatted distance using a helper, or display "Nearby" if distance isn't in the model yet:

```kotlin
@Composable
private fun NearbyUserCard(user: NearbyUser) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, NotionColors.Divider, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Anonymous avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NotionColors.SurfaceSecondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = NotionColors.TextSecondary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "User ${user.userId.take(8)}",
                style = MaterialTheme.typography.titleSmall,
                color = NotionColors.TextPrimary
            )
            Text(
                text = "Nearby",
                style = MaterialTheme.typography.bodySmall,
                color = NotionColors.TextTertiary
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = NotionColors.TextTertiary
        )
    }
}
```

**Step 4: Update section header in `NearbyUsersContent`**

Replace the plain `Text("${nearbyUsers.size} users nearby")` with Notion-style header:
```kotlin
Text(
    text = "${nearbyUsers.size} PEOPLE NEARBY",
    style = DockifyTextStyles.sectionHeader,
    color = NotionColors.TextTertiary,
    modifier = Modifier.padding(vertical = 8.dp)
)
```

**Step 5: Update `YourLocation` card to use Notion style**

Replace the `primaryContainer` color card with:
```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.surface)
        .border(1.dp, NotionColors.Divider, RoundedCornerShape(12.dp))
        .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(NotionColors.StatusSuccess)
    )
    Text(
        text = "Your location is active",
        style = MaterialTheme.typography.bodySmall,
        color = NotionColors.TextSecondary
    )
}
```

**Step 6: Build**

```bash
./gradlew :composeApp:assembleDebug 2>&1 | tail -20
```
Expected: BUILD SUCCESSFUL

**Step 7: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/dockify/features/location/presentation/nearby/NearbyScreen.kt
git commit -m "feat: redesign Nearby screen with profile avatar and Notion-style cards"
```

---

## Task 12: Run Lint + Final Build Verification

**Step 1: Run lint**

```bash
./gradlew :composeApp:lint 2>&1 | tail -30
```

**Step 2: Run unit tests**

```bash
./gradlew :composeApp:testDebugUnitTest 2>&1 | tail -20
```

**Step 3: Final build**

```bash
./gradlew :composeApp:assembleDebug 2>&1 | tail -10
```
Expected: BUILD SUCCESSFUL

**Step 4: Final commit if any lint fixes were made**

```bash
git add -A
git commit -m "chore: fix lint warnings from UI redesign"
```

---

## Summary of Changes

| File | Change |
|------|--------|
| `ui/theme/Color.kt` | Added `NotionColors` semantic tokens |
| `ui/theme/Theme.kt` | Updated `LightColorScheme` to Notion palette |
| `ui/theme/Dimensions.kt` | Updated card radius (12dp), elevation (0dp), added border/avatar tokens |
| `ui/theme/Type.kt` | Updated metric sizes, added `DockifyTextStyles.sectionHeader` |
| `ui/navigation/Routes.kt` | Removed `HomeRoute`/`MapRoute`/`HealthDashboardRoute`; added `HealthRoute`; 2-tab `TopLevelDestination` |
| `ui/navigation/AppNavigator.kt` | Removed stale methods; `navigateToHealthAfterLogin()` replaces old home method |
| `ui/navigation/MainScaffold.kt` | 2-tab `BottomNavItem` enum; removed `PlaceholderContent` |
| `App.kt` | Wired new routes; passes nav callbacks to screens |
| `health/presentation/HealthScreen.kt` | Added nav params; new layout order (StatusOverview → Vitals → AI → Activity) |
| `health/presentation/components/StatusOverviewCard.kt` | New — hero status grid |
| `health/presentation/components/HealthVitalsGrid.kt` | Notion-style card borders, no colored backgrounds |
| `location/presentation/nearby/NearbyScreen.kt` | Profile avatar top bar; distance user cards; Notion-style section header |
