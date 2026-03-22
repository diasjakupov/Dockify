package io.diasjakupov.dockify.features.documents.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.ui.theme.Mint10
import io.diasjakupov.dockify.ui.theme.Mint20
import io.diasjakupov.dockify.ui.theme.Navy20
import io.diasjakupov.dockify.ui.theme.Navy30
import io.diasjakupov.dockify.ui.theme.SoftBlue10
import io.diasjakupov.dockify.ui.theme.SoftBlue20
import kotlinx.coroutines.launch

private object Strings {
    const val TITLE = "Add Document"
    const val CANCEL = "Cancel"
    const val CAMERA_LABEL = "Camera"
    const val CAMERA_SUBLABEL = "Take photo"
    const val GALLERY_LABEL = "Gallery"
    const val GALLERY_SUBLABEL = "Pick image"
    const val FILES_LABEL = "Files"
    const val FILES_SUBLABEL = "Browse"
    const val RECENT = "Recent"
    const val FORMATS_HINT = "Supported: PDF, Images, DOCX, XLSX"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentBottomSheet(
    recentDocuments: List<Document>,
    onPickFromCamera: () -> Unit,
    onPickFromGallery: () -> Unit,
    onPickFromFiles: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // Title row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.TITLE,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    }
                ) {
                    Text(Strings.CANCEL, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Source cards row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SourceCard(
                    icon = Icons.Default.PhotoCamera,
                    label = Strings.CAMERA_LABEL,
                    sublabel = Strings.CAMERA_SUBLABEL,
                    gradient = Brush.linearGradient(listOf(Navy20, Navy30)),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onPickFromCamera(); onDismiss() }
                    }
                )
                SourceCard(
                    icon = Icons.Default.Image,
                    label = Strings.GALLERY_LABEL,
                    sublabel = Strings.GALLERY_SUBLABEL,
                    gradient = Brush.linearGradient(listOf(SoftBlue10, SoftBlue20)),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onPickFromGallery(); onDismiss() }
                    }
                )
                SourceCard(
                    icon = Icons.Default.FolderOpen,
                    label = Strings.FILES_LABEL,
                    sublabel = Strings.FILES_SUBLABEL,
                    gradient = Brush.linearGradient(listOf(Mint10, Mint20)),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onPickFromFiles(); onDismiss() }
                    }
                )
            }

            // Recent row (if any documents exist)
            if (recentDocuments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = Strings.RECENT,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentDocuments.take(3)) { doc ->
                        RecentDocumentThumbnail(document = doc)
                    }
                }
            }

            // Supported formats hint
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = Strings.FORMATS_HINT,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SourceCard(
    icon: ImageVector,
    label: String,
    sublabel: String,
    gradient: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(gradient)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(28.dp),
                tint = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = sublabel,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun RecentDocumentThumbnail(document: Document) {
    val icon = when {
        document.contentType.contains("image") -> Icons.Default.Image
        document.contentType.contains("pdf") -> Icons.Default.Description
        document.contentType.contains("word") || document.contentType.contains("docx") -> Icons.Default.Description
        document.contentType.contains("sheet") || document.contentType.contains("xlsx") -> Icons.AutoMirrored.Filled.InsertDriveFile
        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = document.fileName,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
