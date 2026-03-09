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
