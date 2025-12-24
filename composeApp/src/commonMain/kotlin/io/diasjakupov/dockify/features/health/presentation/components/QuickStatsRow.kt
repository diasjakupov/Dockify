package io.diasjakupov.dockify.features.health.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.features.health.domain.model.HealthMetric
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType
import io.diasjakupov.dockify.ui.theme.Mint50
import io.diasjakupov.dockify.ui.theme.Navy40
import io.diasjakupov.dockify.ui.theme.SoftBlue50

/**
 * Quick stats data model for displaying in the row.
 */
data class QuickStat(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val unit: String,
    val color: Color,
    val trend: TrendDirection = TrendDirection.NEUTRAL
)

enum class TrendDirection {
    UP, DOWN, NEUTRAL
}

/**
 * Horizontal scrollable row of quick stats inspired by Google Fit's cards.
 */
@Composable
fun QuickStatsRow(
    metrics: List<HealthMetric>,
    modifier: Modifier = Modifier
) {
    val stats = metrics.toQuickStats()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stats.take(3).forEach { stat ->
            QuickStatCard(
                stat = stat,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickStatCard(
    stat: QuickStat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(stat.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stat.icon,
                    contentDescription = null,
                    tint = stat.color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stat.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stat.value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stat.unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

/**
 * Compact quick stat for inline display.
 */
@Composable
fun CompactQuickStat(
    icon: ImageVector,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun List<HealthMetric>.toQuickStats(): List<QuickStat> {
    return mapNotNull { metric ->
        when (metric.type) {
            HealthMetricType.STEPS -> QuickStat(
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                label = "Steps",
                value = formatStatValue(metric.value),
                unit = "",
                color = Navy40
            )
            HealthMetricType.CALORIES_BURNED -> QuickStat(
                icon = Icons.Default.LocalFireDepartment,
                label = "Calories",
                value = formatStatValue(metric.value),
                unit = "kcal",
                color = Mint50
            )
            HealthMetricType.DISTANCE -> QuickStat(
                icon = Icons.Default.Route,
                label = "Distance",
                value = metric.value.formatOneDecimal(),
                unit = metric.unit,
                color = SoftBlue50
            )
            HealthMetricType.HEART_RATE -> QuickStat(
                icon = Icons.Default.Favorite,
                label = "Heart Rate",
                value = metric.value.toInt().toString(),
                unit = metric.unit,
                color = Color(0xFFE53935)
            )
            HealthMetricType.SLEEP_DURATION -> QuickStat(
                icon = Icons.Default.Bedtime,
                label = "Sleep",
                value = metric.value.formatOneDecimal(),
                unit = metric.unit,
                color = Color(0xFF7E57C2)
            )
            else -> null
        }
    }
}

private fun formatStatValue(value: Double): String {
    return when {
        value >= 1_000_000 -> "${(value / 1_000_000).formatOneDecimal()}M"
        value >= 10_000 -> "${(value / 1_000).formatOneDecimal()}K"
        value >= 1_000 -> "${(value / 1_000).formatOneDecimal()}K"
        value == value.toLong().toDouble() -> value.toLong().toString()
        else -> value.formatOneDecimal()
    }
}

private fun Double.formatOneDecimal(): String {
    val rounded = (this * 10).toLong() / 10.0
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}
