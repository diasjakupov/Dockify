package io.diasjakupov.dockify.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

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

private val DarkColorScheme = darkColorScheme(
    primary = Navy80,
    onPrimary = Navy20,
    primaryContainer = Navy30,
    onPrimaryContainer = Navy90,

    secondary = Mint80,
    onSecondary = Mint20,
    secondaryContainer = Mint30,
    onSecondaryContainer = Mint90,

    tertiary = SoftBlue80,
    onTertiary = SoftBlue20,
    tertiaryContainer = SoftBlue30,
    onTertiaryContainer = SoftBlue90,

    error = ErrorRed80,
    onError = ErrorRed20,
    errorContainer = ErrorRed30,
    onErrorContainer = ErrorRed90,

    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80,

    outline = NeutralVariant60,
    outlineVariant = NeutralVariant30,
    scrim = Neutral10
)

/**
 * Extended colors for health-specific UI elements.
 */
data class ExtendedColors(
    val excellent: Color,
    val good: Color,
    val normal: Color,
    val warning: Color,
    val alert: Color,
    val critical: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        excellent = HealthStatusColors.Excellent,
        good = HealthStatusColors.Good,
        normal = HealthStatusColors.Normal,
        warning = HealthStatusColors.Warning,
        alert = HealthStatusColors.Alert,
        critical = HealthStatusColors.Critical
    )
}

/**
 * Dockify app theme composable.
 * Wraps content with Material3 theme and provides extended colors and dimensions.
 */
@Composable
fun DockifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = ExtendedColors(
        excellent = HealthStatusColors.Excellent,
        good = HealthStatusColors.Good,
        normal = HealthStatusColors.Normal,
        warning = HealthStatusColors.Warning,
        alert = HealthStatusColors.Alert,
        critical = HealthStatusColors.Critical
    )

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
        LocalDimensions provides Dimensions()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DockifyTypography,
            shapes = DockifyShapes,
            content = content
        )
    }
}

/**
 * Object for accessing theme properties outside of composables.
 */
object DockifyTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current

    val dimensions: Dimensions
        @Composable
        get() = LocalDimensions.current
}
