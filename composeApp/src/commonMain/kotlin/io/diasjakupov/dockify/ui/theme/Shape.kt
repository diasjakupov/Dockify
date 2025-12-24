package io.diasjakupov.dockify.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val DockifyShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * Additional custom shapes for health-specific UI components.
 */
object HealthShapes {
    val metricCard = RoundedCornerShape(20.dp)
    val progressIndicator = RoundedCornerShape(50)
    val bottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val chip = RoundedCornerShape(8.dp)
    val button = RoundedCornerShape(12.dp)
    val inputField = RoundedCornerShape(12.dp)
    val dialog = RoundedCornerShape(28.dp)
}
