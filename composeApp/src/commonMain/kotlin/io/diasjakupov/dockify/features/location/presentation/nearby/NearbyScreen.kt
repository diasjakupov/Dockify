package io.diasjakupov.dockify.features.location.presentation.nearby

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.features.location.domain.model.NearbyUser
import io.diasjakupov.dockify.features.location.permission.LocationPermissionEffect
import io.diasjakupov.dockify.features.location.permission.LocationPermissionHandler
import io.diasjakupov.dockify.ui.components.common.DockifyScaffold
import io.diasjakupov.dockify.ui.components.common.TopBarConfig
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Nearby screen displaying nearby users based on location.
 */
@Composable
fun NearbyScreen(
    onNavigateToProfile: () -> Unit = {},
    viewModel: NearbyViewModel = koinViewModel(),
    permissionHandler: LocationPermissionHandler = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Platform-specific permission handling
    LocationPermissionEffect(permissionHandler = permissionHandler)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NearbyEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is NearbyEffect.OpenGpsSettings -> {
                    // Platform-specific GPS settings navigation
                }
                is NearbyEffect.LocationFetched -> {
                    // Location successfully fetched
                }
            }
        }
    }

    DockifyScaffold(
        topBarConfig = TopBarConfig.Custom {
            NearbyTopBar(
                isRefreshing = state.isManualRefreshing,
                canRefresh = state.canRefresh,
                onRefresh = { viewModel.onAction(NearbyAction.RefreshNearbyUsers) },
                onProfileClick = onNavigateToProfile
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    LoadingContent()
                }
                state.needsPermission -> {
                    PermissionRequiredContent(
                        onRequestPermission = { viewModel.onAction(NearbyAction.RequestPermission) }
                    )
                }
                state.isGpsDisabled -> {
                    GpsDisabledContent(
                        onOpenSettings = { viewModel.onAction(NearbyAction.OpenLocationSettings) }
                    )
                }
                state.error != null -> {
                    ErrorContent(
                        errorMessage = state.error!!,
                        onRetry = { viewModel.onAction(NearbyAction.RetryLastAction) },
                        onDismiss = { viewModel.onAction(NearbyAction.DismissError) }
                    )
                }
                state.hasNearbyUsers -> {
                    NearbyUsersContent(
                        nearbyUsers = state.nearbyUsers,
                        currentLocation = state.currentLocation
                    )
                }
                else -> {
                    EmptyContent()
                }
            }
        }
    }
}

@Composable
private fun NearbyTopBar(
    isRefreshing: Boolean,
    canRefresh: Boolean,
    onRefresh: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Nearby",
            style = MaterialTheme.typography.titleLarge,
            color = io.diasjakupov.dockify.ui.theme.NotionColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRefresh, enabled = canRefresh) {
            if (isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = io.diasjakupov.dockify.ui.theme.NotionColors.TextSecondary
                )
            }
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(io.diasjakupov.dockify.ui.theme.NotionColors.SurfaceSecondary)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier.size(20.dp),
                tint = io.diasjakupov.dockify.ui.theme.NotionColors.TextSecondary
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Getting your location...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PermissionRequiredContent(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Location Permission Required",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We need your location to find nearby users and help you connect with others in your area.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRequestPermission) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
private fun GpsDisabledContent(
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Location Services Disabled",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please enable GPS/Location services to find nearby users.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Open Settings")
            }
        }
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Try Again")
            }
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Nearby Users",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "There are no users nearby at the moment. Try again later or expand your search area.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NearbyUsersContent(
    nearbyUsers: List<NearbyUser>,
    currentLocation: io.diasjakupov.dockify.features.location.domain.model.Location?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Location status row
        currentLocation?.let {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, io.diasjakupov.dockify.ui.theme.NotionColors.Divider, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(io.diasjakupov.dockify.ui.theme.NotionColors.StatusSuccess)
                    )
                    Text(
                        text = "Your location is active",
                        style = MaterialTheme.typography.bodySmall,
                        color = io.diasjakupov.dockify.ui.theme.NotionColors.TextSecondary
                    )
                }
            }
        }

        // Section header
        item {
            Text(
                text = "${nearbyUsers.size} PEOPLE NEARBY",
                style = io.diasjakupov.dockify.ui.theme.DockifyTextStyles.sectionHeader,
                color = io.diasjakupov.dockify.ui.theme.NotionColors.TextTertiary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Nearby users list
        items(nearbyUsers) { user ->
            NearbyUserCard(user = user)
        }
    }
}

@Composable
private fun NearbyUserCard(user: NearbyUser) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, io.diasjakupov.dockify.ui.theme.NotionColors.Divider, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(io.diasjakupov.dockify.ui.theme.NotionColors.SurfaceSecondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = io.diasjakupov.dockify.ui.theme.NotionColors.TextSecondary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "User ${user.userId.take(8)}",
                style = MaterialTheme.typography.titleSmall,
                color = io.diasjakupov.dockify.ui.theme.NotionColors.TextPrimary
            )
            Text(
                text = "Nearby",
                style = MaterialTheme.typography.bodySmall,
                color = io.diasjakupov.dockify.ui.theme.NotionColors.TextTertiary
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = io.diasjakupov.dockify.ui.theme.NotionColors.TextTertiary
        )
    }
}

/**
 * Formats a coordinate value to 4 decimal places.
 * Kotlin Multiplatform compatible alternative to String.format.
 */
private fun formatCoordinate(value: Double): String {
    val rounded = kotlin.math.round(value * 10000) / 10000
    return rounded.toString()
}
