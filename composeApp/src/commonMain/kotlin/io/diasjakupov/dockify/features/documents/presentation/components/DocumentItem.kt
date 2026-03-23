package io.diasjakupov.dockify.features.documents.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.ui.theme.ErrorRed50
import io.diasjakupov.dockify.ui.theme.Mint50
import io.diasjakupov.dockify.ui.theme.Navy40
import io.diasjakupov.dockify.ui.theme.SoftBlue50
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentItem(
    document: Document,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onSwipeDelete: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showDelete by remember { mutableStateOf(false) }
    val typeInfo = fileTypeInfo(document.contentType)

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val swipeState = remember {
        AnchoredDraggableState(initialValue = SwipeAnchor.Idle)
    }

    // Collapse long-press delete icon when swiping begins
    LaunchedEffect(swipeState.currentValue) {
        if (swipeState.currentValue != SwipeAnchor.Idle) {
            showDelete = false
        }
    }

    // Trigger direct delete after full swipe settles
    var swipeDeleteDispatched by remember { mutableStateOf(false) }

    LaunchedEffect(swipeState.settledValue) {
        if (swipeState.settledValue == SwipeAnchor.Dismissed && !swipeDeleteDispatched) {
            swipeDeleteDispatched = true
            onSwipeDelete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        // ── Background layer (static, always behind foreground) ──────────────
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        scope.launch { swipeState.animateTo(SwipeAnchor.Idle) }
                        onDelete()
                    },
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // ── Foreground layer (slides left) ───────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { size ->
                    val revealedPx = -with(density) { 80.dp.toPx() }
                    val dismissedPx = -size.width.toFloat()
                    swipeState.updateAnchors(
                        DraggableAnchors {
                            SwipeAnchor.Idle at 0f
                            SwipeAnchor.Revealed at revealedPx
                            SwipeAnchor.Dismissed at dismissedPx
                        }
                    )
                }
                .offset { IntOffset(swipeState.requireOffset().roundToInt(), 0) }
                .anchoredDraggable(
                    state = swipeState,
                    orientation = Orientation.Horizontal,
                    enabled = enabled
                )
                .combinedClickable(
                    enabled = enabled,
                    onClick = {
                        when {
                            swipeState.currentValue == SwipeAnchor.Revealed ->
                                scope.launch { swipeState.animateTo(SwipeAnchor.Idle) }
                            showDelete -> showDelete = false
                            else -> onOpen()
                        }
                    },
                    onLongClick = { showDelete = true }
                ),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left accent stripe — color-coded by file type
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(typeInfo.accentColor)
                )

                Spacer(Modifier.width(12.dp))

                // File type badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(typeInfo.accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = typeInfo.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = typeInfo.accentColor
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 14.dp)
                ) {
                    Text(
                        text = document.fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "${document.fileSize.toReadableSize()} · ${document.uploadedAt.take(10)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = showDelete,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f)
                ) {
                    IconButton(
                        onClick = { showDelete = false; onDelete() },
                        enabled = enabled
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (!showDelete) Spacer(Modifier.width(8.dp))
            }
        }
    }
}

private data class FileTypeInfo(val label: String, val accentColor: Color)

private fun fileTypeInfo(contentType: String): FileTypeInfo = when {
    contentType.startsWith("image") -> FileTypeInfo("IMG", SoftBlue50)
    contentType.contains("pdf") -> FileTypeInfo("PDF", ErrorRed50)
    contentType.contains("word") || contentType.contains("docx") -> FileTypeInfo("DOC", Navy40)
    contentType.contains("sheet") || contentType.contains("xlsx") -> FileTypeInfo("XLS", Mint50)
    else -> FileTypeInfo("FILE", SoftBlue50)
}

private fun Long.toReadableSize(): String = when {
    this >= 1024 * 1024 -> "%.1f MB".format(this / (1024.0 * 1024.0))
    this >= 1024 -> "%.1f KB".format(this / 1024.0)
    else -> "$this B"
}

private enum class SwipeAnchor { Idle, Revealed, Dismissed }
