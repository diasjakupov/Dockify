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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MonitorHeart
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.features.health.domain.model.HealthMetric
import io.diasjakupov.dockify.features.health.domain.model.HealthMetricType
import io.diasjakupov.dockify.ui.theme.DockifyTheme
import io.diasjakupov.dockify.ui.theme.HealthStatusColors

/**
 * Health status level for vital signs.
 */
enum class VitalStatus {
    EXCELLENT, GOOD, NORMAL, WARNING, ALERT, CRITICAL
}

/**
 * Data class for vital sign display.
 */
data class VitalSign(
    val type: HealthMetricType,
    val icon: ImageVector,
    val label: String,
    val value: String,
    val unit: String,
    val status: VitalStatus,
    val statusLabel: String,
    val accentColor: Color
)

/**
 * Grid layout for displaying health vitals inspired by Apple Health.
 */
@Composable
fun HealthVitalsSection(
    metrics: List<HealthMetric>,
    modifier: Modifier = Modifier
) {
    val vitals = metrics.toVitalSigns()

    if (vitals.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Health Vitals",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2x2 Grid layout
        val rows = vitals.chunked(2)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { vital ->
                        VitalCard(
                            vital = vital,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill empty space if odd number
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun VitalCard(
    vital: VitalSign,
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(vital.accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = vital.icon,
                        contentDescription = null,
                        tint = vital.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Status indicator
                StatusBadge(
                    status = vital.status,
                    label = vital.statusLabel
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = vital.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = vital.value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = vital.unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: VitalStatus,
    label: String,
    modifier: Modifier = Modifier
) {
    val statusColor = when (status) {
        VitalStatus.EXCELLENT -> HealthStatusColors.Excellent
        VitalStatus.GOOD -> HealthStatusColors.Good
        VitalStatus.NORMAL -> HealthStatusColors.Normal
        VitalStatus.WARNING -> HealthStatusColors.Warning
        VitalStatus.ALERT -> HealthStatusColors.Alert
        VitalStatus.CRITICAL -> HealthStatusColors.Critical
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(statusColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = statusColor
            )
        }
    }
}

/**
 * Large vital card for primary metrics (e.g., heart rate).
 */
@Composable
fun LargeVitalCard(
    vital: VitalSign,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = vital.accentColor.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = vital.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = vital.accentColor.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = vital.value,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = vital.accentColor
                    )
                    Text(
                        text = vital.unit,
                        style = MaterialTheme.typography.titleMedium,
                        color = vital.accentColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                StatusBadge(
                    status = vital.status,
                    label = vital.statusLabel
                )
            }

            Icon(
                imageVector = vital.icon,
                contentDescription = null,
                tint = vital.accentColor.copy(alpha = 0.3f),
                modifier = Modifier.size(80.dp)
            )
        }
    }
}

private fun List<HealthMetric>.toVitalSigns(): List<VitalSign> {
    return mapNotNull { metric ->
        when (metric.type) {
            HealthMetricType.HEART_RATE -> VitalSign(
                type = metric.type,
                icon = Icons.Default.Favorite,
                label = "Heart Rate",
                value = metric.value.toInt().toString(),
                unit = metric.unit,
                status = getHeartRateStatus(metric.value),
                statusLabel = getHeartRateStatusLabel(metric.value),
                accentColor = Color(0xFFE53935)
            )
            HealthMetricType.BLOOD_OXYGEN -> VitalSign(
                type = metric.type,
                icon = Icons.Default.Air,
                label = "Blood Oxygen",
                value = metric.value.toInt().toString(),
                unit = metric.unit,
                status = getBloodOxygenStatus(metric.value),
                statusLabel = getBloodOxygenStatusLabel(metric.value),
                accentColor = Color(0xFF42A5F5)
            )
            HealthMetricType.SLEEP_DURATION -> VitalSign(
                type = metric.type,
                icon = Icons.Default.Bedtime,
                label = "Sleep",
                value = metric.value.formatOneDecimal(),
                unit = metric.unit,
                status = getSleepStatus(metric.value),
                statusLabel = getSleepStatusLabel(metric.value),
                accentColor = Color(0xFF7E57C2)
            )
            HealthMetricType.BLOOD_PRESSURE_SYSTOLIC -> VitalSign(
                type = metric.type,
                icon = Icons.Default.MonitorHeart,
                label = "Blood Pressure",
                value = metric.value.toInt().toString(),
                unit = metric.unit,
                status = getBloodPressureStatus(metric.value),
                statusLabel = getBloodPressureStatusLabel(metric.value),
                accentColor = Color(0xFFFF7043)
            )
            HealthMetricType.WEIGHT -> VitalSign(
                type = metric.type,
                icon = Icons.Default.Scale,
                label = "Weight",
                value = metric.value.formatOneDecimal(),
                unit = metric.unit,
                status = VitalStatus.NORMAL,
                statusLabel = "Tracked",
                accentColor = Color(0xFF26A69A)
            )
            HealthMetricType.BODY_TEMPERATURE -> VitalSign(
                type = metric.type,
                icon = Icons.Default.Thermostat,
                label = "Temperature",
                value = metric.value.formatOneDecimal(),
                unit = metric.unit,
                status = getTemperatureStatus(metric.value),
                statusLabel = getTemperatureStatusLabel(metric.value),
                accentColor = Color(0xFFFFB300)
            )
            else -> null
        }
    }
}

// Status determination functions
private fun getHeartRateStatus(bpm: Double): VitalStatus = when {
    bpm < 40 || bpm > 120 -> VitalStatus.ALERT
    bpm < 50 || bpm > 100 -> VitalStatus.WARNING
    bpm in 60.0..80.0 -> VitalStatus.EXCELLENT
    else -> VitalStatus.NORMAL
}

private fun getHeartRateStatusLabel(bpm: Double): String = when {
    bpm < 40 -> "Very Low"
    bpm > 120 -> "High"
    bpm < 50 -> "Low"
    bpm > 100 -> "Elevated"
    bpm in 60.0..80.0 -> "Optimal"
    else -> "Normal"
}

private fun getBloodOxygenStatus(spo2: Double): VitalStatus = when {
    spo2 < 90 -> VitalStatus.CRITICAL
    spo2 < 94 -> VitalStatus.ALERT
    spo2 < 96 -> VitalStatus.WARNING
    spo2 >= 98 -> VitalStatus.EXCELLENT
    else -> VitalStatus.GOOD
}

private fun getBloodOxygenStatusLabel(spo2: Double): String = when {
    spo2 < 90 -> "Critical"
    spo2 < 94 -> "Low"
    spo2 < 96 -> "Below Normal"
    spo2 >= 98 -> "Excellent"
    else -> "Normal"
}

private fun getSleepStatus(hours: Double): VitalStatus = when {
    hours < 5 -> VitalStatus.ALERT
    hours < 6 -> VitalStatus.WARNING
    hours in 7.0..9.0 -> VitalStatus.EXCELLENT
    hours > 10 -> VitalStatus.WARNING
    else -> VitalStatus.GOOD
}

private fun getSleepStatusLabel(hours: Double): String = when {
    hours < 5 -> "Too Short"
    hours < 6 -> "Short"
    hours in 7.0..9.0 -> "Optimal"
    hours > 10 -> "Long"
    else -> "Good"
}

private fun getBloodPressureStatus(systolic: Double): VitalStatus = when {
    systolic < 90 -> VitalStatus.WARNING
    systolic > 140 -> VitalStatus.ALERT
    systolic > 130 -> VitalStatus.WARNING
    systolic in 110.0..120.0 -> VitalStatus.EXCELLENT
    else -> VitalStatus.NORMAL
}

private fun getBloodPressureStatusLabel(systolic: Double): String = when {
    systolic < 90 -> "Low"
    systolic > 140 -> "High"
    systolic > 130 -> "Elevated"
    systolic in 110.0..120.0 -> "Optimal"
    else -> "Normal"
}

private fun getTemperatureStatus(temp: Double): VitalStatus = when {
    temp < 35.5 -> VitalStatus.WARNING
    temp > 38.0 -> VitalStatus.ALERT
    temp > 37.5 -> VitalStatus.WARNING
    temp in 36.1..37.2 -> VitalStatus.EXCELLENT
    else -> VitalStatus.NORMAL
}

private fun getTemperatureStatusLabel(temp: Double): String = when {
    temp < 35.5 -> "Low"
    temp > 38.0 -> "Fever"
    temp > 37.5 -> "Elevated"
    temp in 36.1..37.2 -> "Normal"
    else -> "Normal"
}

private fun Double.formatOneDecimal(): String {
    val value = (this * 10).toLong() / 10.0
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}
