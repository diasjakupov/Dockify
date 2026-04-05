package io.diasjakupov.dockify.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Description
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
import androidx.compose.ui.unit.dp
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
    ),
    DOCUMENTS(
        route = DocumentsRoute,
        label = "Documents",
        selectedIcon = Icons.Filled.Description,
        unselectedIcon = Icons.Outlined.Description
    ),
    CHAT(
        route = ChatRoute(),
        label = "Chat",
        selectedIcon = Icons.Filled.ChatBubble,
        unselectedIcon = Icons.Outlined.ChatBubbleOutline
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
        contentWindowInsets = WindowInsets(0),
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
        tonalElevation = 0.dp
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
