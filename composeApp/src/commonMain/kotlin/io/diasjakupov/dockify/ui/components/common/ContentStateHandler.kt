package io.diasjakupov.dockify.ui.components.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.ui.theme.DockifyTheme

/**
 * Represents the state of content loading.
 */
sealed class ContentState<out T> {
    data object Loading : ContentState<Nothing>()
    data class Error(val message: String, val retry: (() -> Unit)? = null) : ContentState<Nothing>()
    data class Empty(val message: String = "No data available") : ContentState<Nothing>()
    data class Success<T>(val data: T) : ContentState<T>()
}

/**
 * A composable that handles different content states (Loading, Error, Empty, Success).
 */
@Composable
fun <T> ContentStateHandler(
    state: ContentState<T>,
    modifier: Modifier = Modifier,
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    errorContent: @Composable (message: String, retry: (() -> Unit)?) -> Unit = { msg, retry ->
        DefaultErrorContent(message = msg, onRetry = retry)
    },
    emptyContent: @Composable (message: String) -> Unit = { msg ->
        DefaultEmptyContent(message = msg)
    },
    successContent: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = state,
        modifier = modifier,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        }
    ) { contentState ->
        when (contentState) {
            is ContentState.Loading -> loadingContent()
            is ContentState.Error -> errorContent(contentState.message, contentState.retry)
            is ContentState.Empty -> emptyContent(contentState.message)
            is ContentState.Success -> successContent(contentState.data)
        }
    }
}

@Composable
fun DefaultLoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DockifyTheme.dimensions.spaceLarge)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DefaultErrorContent(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DockifyTheme.dimensions.spaceLarge),
            modifier = Modifier.padding(DockifyTheme.dimensions.screenPaddingHorizontal)
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = "Error",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (onRetry != null) {
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
fun DefaultEmptyContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DockifyTheme.dimensions.spaceLarge),
            modifier = Modifier.padding(DockifyTheme.dimensions.screenPaddingHorizontal)
        ) {
            Icon(
                imageVector = Icons.Outlined.Inbox,
                contentDescription = "Empty",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Loading overlay that can be shown over existing content.
 */
@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
