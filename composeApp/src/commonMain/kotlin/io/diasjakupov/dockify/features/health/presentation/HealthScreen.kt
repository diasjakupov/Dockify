package io.diasjakupov.dockify.features.health.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.features.health.permission.HealthPermissionEffect
import io.diasjakupov.dockify.features.health.permission.HealthPermissionHandler
import io.diasjakupov.dockify.features.location.permission.LocationPermissionEffect
import io.diasjakupov.dockify.features.location.permission.LocationPermissionHandler
import io.diasjakupov.dockify.features.health.presentation.components.ActivityRingCard
import io.diasjakupov.dockify.features.health.presentation.components.HealthVitalsSection
import io.diasjakupov.dockify.features.health.presentation.components.RecommendationCard
import io.diasjakupov.dockify.features.health.presentation.components.TodaySummaryCard
import io.diasjakupov.dockify.ui.components.common.DockifyScaffold
import io.diasjakupov.dockify.ui.components.common.TopBarConfig
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Health screen displaying a modern health dashboard inspired by Google Fit,
 * Apple Health, Hevy, and BetterMe.
 */
@Composable
fun HealthScreen(
    viewModel: HealthViewModel = koinViewModel(),
    healthPermissionHandler: HealthPermissionHandler = koinInject(),
    locationPermissionHandler: LocationPermissionHandler = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Platform-specific permission handling for Health Connect/HealthKit
    HealthPermissionEffect(permissionHandler = healthPermissionHandler)

    // Platform-specific permission handling for Location
    LocationPermissionEffect(permissionHandler = locationPermissionHandler)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HealthEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is HealthEffect.SyncSuccess -> {
                    // Additional success handling if needed
                }
                is HealthEffect.BackgroundSyncFailed -> {
                    // Background sync failed - error banner will be shown via state
                    // No snackbar needed as the banner provides retry/dismiss options
                }
            }
        }
    }

    DockifyScaffold(
        topBarConfig = TopBarConfig.Custom {
            Column {
                HealthTopBar(
                    isSyncing = state.isSyncing,
                    canSync = state.canSync,
                    onSync = { viewModel.onAction(HealthAction.SyncHealthData) }
                )
                BackgroundSyncStatusBanner(
                    isBackgroundSyncing = state.isBackgroundSyncing,
                    backgroundSyncError = state.backgroundSyncError,
                    onRetry = { viewModel.onAction(HealthAction.RetryBackgroundSync) },
                    onDismiss = { viewModel.onAction(HealthAction.DismissBackgroundSyncError) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        HealthScreenContent(
            state = state,
            onAction = viewModel::onAction,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun HealthTopBar(
    isSyncing: Boolean,
    canSync: Boolean,
    onSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Health",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        IconButton(
            onClick = onSync,
            enabled = canSync,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync Health Data",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Background sync status banner that shows:
 * - "Syncing to cloud..." with spinner when syncing in background
 * - Error with Retry/Dismiss buttons when background sync fails
 */
@Composable
private fun BackgroundSyncStatusBanner(
    isBackgroundSyncing: Boolean,
    backgroundSyncError: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showBanner = isBackgroundSyncing || backgroundSyncError != null

    AnimatedVisibility(
        visible = showBanner,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = if (backgroundSyncError != null) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isBackgroundSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Syncing...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else if (backgroundSyncError != null) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Sync failed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                if (backgroundSyncError != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TextButton(
                            onClick = onRetry,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = "Retry",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthScreenContent(
    state: HealthState,
    onAction: (HealthAction) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading && state.healthMetrics.isEmpty() -> {
            LoadingContent(modifier = modifier)
        }
        state.error != null && state.healthMetrics.isEmpty() -> {
            ErrorContent(
                error = state.error,
                onRetry = { onAction(HealthAction.RetryLastAction) },
                modifier = modifier
            )
        }
        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Permission request card if needed
                if (state.needsPermission) {
                    item(key = "permission") {
                        PermissionRequestCard(
                            permissionState = state.permissionState,
                            isPlatformAvailable = state.isPlatformHealthAvailable,
                            onRequestPermission = { onAction(HealthAction.RequestPermissions) }
                        )
                    }
                }

                // Today's Summary Card (Greeting + Streak + Goals)
                item(key = "summary") {
                    TodaySummaryCard(summary = state.todaySummary)
                }

                // Activity Rings Card
                if (state.hasMetrics || !state.needsPermission) {
                    item(key = "activity_rings") {
                        ActivityRingCard(progress = state.activityProgress)
                    }
                }

                // AI Recommendation Card
                item(key = "recommendation") {
                    RecommendationCard(
                        recommendation = state.recommendation,
                        isLoading = state.isRecommendationLoading,
                        onRefresh = { onAction(HealthAction.RefreshRecommendation) }
                    )
                }

                // Health Vitals Section
                if (state.vitalMetrics.isNotEmpty()) {
                    item(key = "vitals") {
                        HealthVitalsSection(metrics = state.vitalMetrics)
                    }
                }

                // Last sync info
                state.lastSyncTimestamp?.let { timestamp ->
                    item(key = "last_sync") {
                        LastSyncInfo(timestamp = timestamp)
                    }
                }

                // Empty state for no metrics
                if (!state.hasMetrics && !state.needsPermission) {
                    item(key = "empty_state") {
                        EmptyMetricsContent(
                            canSync = state.canSync,
                            onSync = { onAction(HealthAction.SyncHealthData) }
                        )
                    }
                }

                // Bottom spacing
                item(key = "bottom_spacing") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
            Text(
                text = "Loading your health data...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LastSyncInfo(
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Last synced: ${formatTimestamp(timestamp)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun PermissionRequestCard(
    permissionState: PermissionState,
    isPlatformAvailable: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDenied = permissionState == PermissionState.Denied
    val containerColor = if (isDenied) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDenied) {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDenied) Icons.Default.Warning else Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = if (isDenied) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        !isPlatformAvailable -> "Health Connect Not Available"
                        isDenied -> "Permission Required"
                        else -> "Enable Health Sync"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when {
                        !isPlatformAvailable -> "Install Health Connect to sync health data."
                        isDenied -> "Grant access in settings to continue."
                        else -> "Connect your health data to unlock insights."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isPlatformAvailable) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDenied) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.secondary
                            }
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = if (isDenied) "Open Settings" else "Connect",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMetricsContent(
    canSync: Boolean,
    onSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Start Tracking Your Health",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sync your health data from your device to see daily activity, vitals, and personalized insights.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (canSync) {
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onSync,
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Sync Health Data",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Something Went Wrong",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

@OptIn(kotlin.time.ExperimentalTime::class)
private fun formatTimestamp(timestamp: Long): String {
    val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
    val diff = now - timestamp
    return when {
        diff < 60_000L -> "Just now"
        diff < 3_600_000L -> "${diff / 60_000L} minutes ago"
        diff < 86_400_000L -> "${diff / 3_600_000L} hours ago"
        else -> "${diff / 86_400_000L} days ago"
    }
}
