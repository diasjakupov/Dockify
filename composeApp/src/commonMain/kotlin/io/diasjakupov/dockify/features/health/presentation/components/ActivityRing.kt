package io.diasjakupov.dockify.features.health.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.ui.theme.Mint50
import io.diasjakupov.dockify.ui.theme.Mint70
import io.diasjakupov.dockify.ui.theme.Navy40
import io.diasjakupov.dockify.ui.theme.Navy60
import io.diasjakupov.dockify.ui.theme.SoftBlue50
import io.diasjakupov.dockify.ui.theme.SoftBlue70

/**
 * Data class representing activity ring progress values.
 */
data class ActivityProgress(
    val steps: Int = 0,
    val stepsGoal: Int = 10000,
    val calories: Int = 0,
    val caloriesGoal: Int = 500,
    val distance: Double = 0.0,
    val distanceGoal: Double = 5.0
)

/**
 * Activity Ring component inspired by Apple Health and Google Fit.
 * Displays concentric rings for steps, calories, and distance progress.
 */
@Composable
fun ActivityRingCard(
    progress: ActivityProgress,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Today's Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Activity Rings
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ActivityRings(
                        stepsProgress = progress.steps.toFloat() / progress.stepsGoal,
                        caloriesProgress = progress.calories.toFloat() / progress.caloriesGoal,
                        distanceProgress = (progress.distance / progress.distanceGoal).toFloat(),
                        modifier = Modifier.fillMaxSize()
                    )

                    // Center content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formatNumber(progress.steps),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "steps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Legend
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActivityLegendItem(
                        icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                        label = "Steps",
                        value = "${formatNumber(progress.steps)} / ${formatNumber(progress.stepsGoal)}",
                        color = Navy40,
                        progress = progress.steps.toFloat() / progress.stepsGoal
                    )
                    ActivityLegendItem(
                        icon = Icons.Default.LocalFireDepartment,
                        label = "Calories",
                        value = "${progress.calories} / ${progress.caloriesGoal} kcal",
                        color = Mint50,
                        progress = progress.calories.toFloat() / progress.caloriesGoal
                    )
                    ActivityLegendItem(
                        icon = Icons.Default.Route,
                        label = "Distance",
                        value = "${progress.distance.formatOneDecimal()} / ${progress.distanceGoal.toInt()} km",
                        color = SoftBlue50,
                        progress = (progress.distance / progress.distanceGoal).toFloat()
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityRings(
    stepsProgress: Float,
    caloriesProgress: Float,
    distanceProgress: Float,
    modifier: Modifier = Modifier
) {
    val stepsAnim = remember { Animatable(0f) }
    val caloriesAnim = remember { Animatable(0f) }
    val distanceAnim = remember { Animatable(0f) }

    LaunchedEffect(stepsProgress) {
        stepsAnim.animateTo(
            targetValue = stepsProgress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(caloriesProgress) {
        caloriesAnim.animateTo(
            targetValue = caloriesProgress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1000, delayMillis = 100, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(distanceProgress) {
        distanceAnim.animateTo(
            targetValue = distanceProgress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1000, delayMillis = 200, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier) {
        val strokeWidth = 14.dp.toPx()
        val gap = 6.dp.toPx()
        val startAngle = -90f

        // Background track color
        val trackColor = Color.Gray.copy(alpha = 0.15f)

        // Outer ring - Steps (Navy)
        val outerRadius = (size.minDimension / 2) - strokeWidth / 2
        val outerSize = Size(outerRadius * 2, outerRadius * 2)
        val outerOffset = Offset(
            (size.width - outerSize.width) / 2,
            (size.height - outerSize.height) / 2
        )

        // Steps track
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = outerOffset,
            size = outerSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Steps progress
        drawArc(
            brush = Brush.sweepGradient(listOf(Navy60, Navy40)),
            startAngle = startAngle,
            sweepAngle = 360f * stepsAnim.value,
            useCenter = false,
            topLeft = outerOffset,
            size = outerSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Middle ring - Calories (Mint)
        val middleRadius = outerRadius - strokeWidth - gap
        val middleSize = Size(middleRadius * 2, middleRadius * 2)
        val middleOffset = Offset(
            (size.width - middleSize.width) / 2,
            (size.height - middleSize.height) / 2
        )

        // Calories track
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = middleOffset,
            size = middleSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Calories progress
        drawArc(
            brush = Brush.sweepGradient(listOf(Mint70, Mint50)),
            startAngle = startAngle,
            sweepAngle = 360f * caloriesAnim.value,
            useCenter = false,
            topLeft = middleOffset,
            size = middleSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Inner ring - Distance (SoftBlue)
        val innerRadius = middleRadius - strokeWidth - gap
        val innerSize = Size(innerRadius * 2, innerRadius * 2)
        val innerOffset = Offset(
            (size.width - innerSize.width) / 2,
            (size.height - innerSize.height) / 2
        )

        // Distance track
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = innerOffset,
            size = innerSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Distance progress
        drawArc(
            brush = Brush.sweepGradient(listOf(SoftBlue70, SoftBlue50)),
            startAngle = startAngle,
            sweepAngle = 360f * distanceAnim.value,
            useCenter = false,
            topLeft = innerOffset,
            size = innerSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun ActivityLegendItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            // Progress circle background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 3.dp.toPx()
                val radius = (size.minDimension / 2) - strokeWidth / 2

                drawCircle(
                    color = color.copy(alpha = 0.2f),
                    radius = radius,
                    style = Stroke(width = strokeWidth)
                )

                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Single activity ring for simpler displays.
 */
@Composable
fun SingleActivityRing(
    progress: Float,
    color: Color,
    trackColor: Color = Color.Gray.copy(alpha = 0.15f),
    strokeWidth: Dp = 8.dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val radius = (size.minDimension / 2) - stroke / 2
            val arcSize = Size(radius * 2, radius * 2)
            val arcOffset = Offset(
                (size.width - arcSize.width) / 2,
                (size.height - arcSize.height) / 2
            )

            // Track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcOffset,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Progress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress.value,
                useCenter = false,
                topLeft = arcOffset,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        content()
    }
}

private fun formatNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> "${(number / 1_000_000.0).formatOneDecimal()}M"
        number >= 1_000 -> "${(number / 1_000.0).formatOneDecimal()}K"
        else -> number.toString()
    }
}

private fun Double.formatOneDecimal(): String {
    val value = (this * 10).toLong() / 10.0
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}
