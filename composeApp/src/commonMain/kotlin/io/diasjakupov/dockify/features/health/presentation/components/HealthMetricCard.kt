package io.diasjakupov.dockify.features.health.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.features.health.domain.model.HealthMetric
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType

/**
 * Card component displaying a single health metric.
 */
@Composable
fun HealthMetricCard(
    metric: HealthMetric,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = metric.type.toIcon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = metric.type.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${metric.value.formatValue()} ${metric.unit}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun HealthMetricType.toIcon(): ImageVector = when (this) {
    HealthMetricType.STEPS -> Icons.AutoMirrored.Filled.DirectionsWalk
    HealthMetricType.HEART_RATE -> Icons.Default.Favorite
    HealthMetricType.BLOOD_PRESSURE_SYSTOLIC,
    HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> Icons.Default.MonitorHeart
    HealthMetricType.BLOOD_OXYGEN -> Icons.Default.Air
    HealthMetricType.SLEEP_DURATION -> Icons.Default.Bedtime
    HealthMetricType.CALORIES_BURNED -> Icons.Default.LocalFireDepartment
    HealthMetricType.DISTANCE -> Icons.Default.Route
    HealthMetricType.WEIGHT -> Icons.Default.Scale
    HealthMetricType.HEIGHT -> Icons.Default.Height
    HealthMetricType.BODY_TEMPERATURE -> Icons.Default.Thermostat
    HealthMetricType.RESPIRATORY_RATE -> Icons.Default.Air
}

private fun Double.formatValue(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        ((this * 10).toLong() / 10.0).toString()
    }
}
