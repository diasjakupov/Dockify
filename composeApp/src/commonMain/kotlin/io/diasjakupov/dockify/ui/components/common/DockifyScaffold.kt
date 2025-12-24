package io.diasjakupov.dockify.ui.components.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Configuration for the top bar in DockifyScaffold.
 */
sealed class TopBarConfig {
    data object None : TopBarConfig()

    data class Simple(
        val title: String,
        val navigationIcon: @Composable (() -> Unit)? = null,
        val actions: @Composable (RowScope.() -> Unit)? = null
    ) : TopBarConfig()

    data class Large(
        val title: String,
        val navigationIcon: @Composable (() -> Unit)? = null,
        val actions: @Composable (RowScope.() -> Unit)? = null
    ) : TopBarConfig()

    data class Custom(
        val content: @Composable () -> Unit
    ) : TopBarConfig()
}

/**
 * Configuration for bottom navigation.
 */
sealed class BottomBarConfig {
    data object None : BottomBarConfig()
    data class Navigation(
        val currentRoute: String,
        val onNavigate: (BottomNavDestination) -> Unit = {}
    ) : BottomBarConfig()
    data class Custom(val content: @Composable () -> Unit) : BottomBarConfig()
}

/**
 * Base scaffold composable for all screens in Dockify.
 * Provides consistent structure with configurable top bar, bottom navigation,
 * and content area.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DockifyScaffold(
    modifier: Modifier = Modifier,
    topBarConfig: TopBarConfig = TopBarConfig.None,
    bottomBarConfig: BottomBarConfig = BottomBarConfig.None,
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    snackbarHost: @Composable () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            when (topBarConfig) {
                is TopBarConfig.None -> {}
                is TopBarConfig.Simple -> {
                    TopAppBar(
                        title = { Text(topBarConfig.title) },
                        navigationIcon = { topBarConfig.navigationIcon?.invoke() },
                        actions = { topBarConfig.actions?.invoke(this) }
                    )
                }
                is TopBarConfig.Large -> {
                    LargeTopAppBar(
                        title = { Text(topBarConfig.title) },
                        navigationIcon = { topBarConfig.navigationIcon?.invoke() },
                        actions = { topBarConfig.actions?.invoke(this) }
                    )
                }
                is TopBarConfig.Custom -> {
                    topBarConfig.content()
                }
            }
        },
        bottomBar = {
            when (bottomBarConfig) {
                is BottomBarConfig.None -> {}
                is BottomBarConfig.Navigation -> {
                    DockifyBottomNavigation(
                        currentRoute = bottomBarConfig.currentRoute,
                        onNavigate = bottomBarConfig.onNavigate
                    )
                }
                is BottomBarConfig.Custom -> {
                    bottomBarConfig.content()
                }
            }
        },
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        snackbarHost = snackbarHost,
        containerColor = containerColor,
        contentWindowInsets = contentWindowInsets,
        content = content
    )
}
