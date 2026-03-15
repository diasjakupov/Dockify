package io.diasjakupov.dockify.features.health.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import io.diasjakupov.dockify.ui.theme.HealthStatusColors
import io.diasjakupov.dockify.ui.theme.NotionColors
import kotlin.math.min

/**
 * Activity progress card displaying steps, calories, and distance with progress bars.
 * Styled in the Notion minimal aesthetic with white card, 1px border, and progress bars.
 */
@Composable
fun ActivityProgressCard(
    progress: ActivityProgress,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ActivityProgressItem(
            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
            label = "Steps",
            displayText = "${progress.steps} / ${progress.stepsGoal} steps",
            fraction = min(progress.steps.toFloat() / progress.stepsGoal, 1f),
            tintColor = HealthStatusColors.Excellent
        )

        ActivityProgressItem(
            icon = Icons.Default.LocalFireDepartment,
            label = "Calories",
            displayText = "${progress.calories} / ${progress.caloriesGoal} kcal",
            fraction = min(progress.calories.toFloat() / progress.caloriesGoal, 1f),
            tintColor = NotionColors.StatusWarning
        )

        ActivityProgressItem(
            icon = Icons.Default.Route,
            label = "Distance",
            displayText = "${"%.1f".format(progress.distance)} / ${"%.1f".format(progress.distanceGoal)} km",
            fraction = min((progress.distance / progress.distanceGoal).toFloat(), 1f),
            tintColor = NotionColors.Accent
        )
    }
}

/**
 * Individual activity progress item with icon, label, progress bar, and stats.
 */
@Composable
private fun ActivityProgressItem(
    icon: ImageVector,
    label: String,
    displayText: String,
    fraction: Float,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(18.dp),
                    tint = tintColor
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = displayText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            color = tintColor
        )
    }
}
