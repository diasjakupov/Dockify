package io.diasjakupov.dockify.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Dimension system for consistent spacing and sizing throughout the app.
 * Based on an 8dp grid system.
 */
data class Dimensions(
    // Base spacing units (8dp grid system)
    val spaceNone: Dp = 0.dp,
    val spaceXXSmall: Dp = 2.dp,
    val spaceXSmall: Dp = 4.dp,
    val spaceSmall: Dp = 8.dp,
    val spaceMedium: Dp = 12.dp,
    val spaceLarge: Dp = 16.dp,
    val spaceXLarge: Dp = 24.dp,
    val spaceXXLarge: Dp = 32.dp,
    val spaceXXXLarge: Dp = 48.dp,
    val spaceHuge: Dp = 64.dp,

    // Screen padding
    val screenPaddingHorizontal: Dp = 16.dp,
    val screenPaddingVertical: Dp = 16.dp,

    // Card dimensions
    val cardPadding: Dp = 16.dp,
    val cardElevation: Dp = 2.dp,
    val cardCornerRadius: Dp = 16.dp,

    // Health metric card dimensions
    val metricCardHeight: Dp = 120.dp,
    val metricCardMinWidth: Dp = 160.dp,
    val metricIconSize: Dp = 48.dp,

    // Bottom navigation
    val bottomNavHeight: Dp = 80.dp,

    // Top bar
    val topBarHeight: Dp = 64.dp,

    // Button dimensions
    val buttonHeight: Dp = 48.dp,
    val buttonMinWidth: Dp = 120.dp,
    val iconButtonSize: Dp = 48.dp,

    // Input fields
    val textFieldHeight: Dp = 56.dp,

    // List items
    val listItemMinHeight: Dp = 56.dp,
    val listItemPadding: Dp = 16.dp,

    // Dividers
    val dividerThickness: Dp = 1.dp,

    // Icons
    val iconSizeSmall: Dp = 16.dp,
    val iconSizeMedium: Dp = 24.dp,
    val iconSizeLarge: Dp = 32.dp,
    val iconSizeXLarge: Dp = 48.dp
)

val LocalDimensions = staticCompositionLocalOf { Dimensions() }
