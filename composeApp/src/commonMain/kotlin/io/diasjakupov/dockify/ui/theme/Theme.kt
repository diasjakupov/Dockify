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
    primary = Navy40,
    onPrimary = Color.White,
    primaryContainer = Navy90,
    onPrimaryContainer = Navy10,

    secondary = Mint40,
    onSecondary = Color.White,
    secondaryContainer = Mint90,
    onSecondaryContainer = Mint10,

    tertiary = SoftBlue40,
    onTertiary = Color.White,
    tertiaryContainer = SoftBlue90,
    onTertiaryContainer = SoftBlue10,

    error = ErrorRed40,
    onError = Color.White,
    errorContainer = ErrorRed90,
    onErrorContainer = ErrorRed10,

    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,

    outline = NeutralVariant50,
    outlineVariant = NeutralVariant80,
    scrim = Neutral10
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
