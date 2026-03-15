package io.diasjakupov.dockify.features.documents.presentation.documents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import io.diasjakupov.dockify.ui.components.common.DockifyScaffold
import io.diasjakupov.dockify.ui.components.common.TopBarConfig
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.features.documents.presentation.components.DocumentItem
import io.diasjakupov.dockify.features.documents.presentation.components.FilePickerBottomSheet
import io.diasjakupov.dockify.features.documents.presentation.components.UploadFab
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DocumentsScreen() {
    val viewModel: DocumentsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Platform file picker launchers
    val filePickerLauncher = rememberFilePickerLauncher { file ->
        if (file != null) viewModel.onAction(DocumentsAction.FileSelected(file))
        else viewModel.onAction(DocumentsAction.PickCancelled)
    }
    val galleryPickerLauncher = rememberGalleryPickerLauncher { file ->
        if (file != null) viewModel.onAction(DocumentsAction.FileSelected(file))
        else viewModel.onAction(DocumentsAction.PickCancelled)
    }

    // Load documents on first composition
    LaunchedEffect(Unit) {
        viewModel.onAction(DocumentsAction.LoadDocuments)
    }

    // Collect effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DocumentsEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is DocumentsEffect.LaunchFilePicker -> filePickerLauncher()
                is DocumentsEffect.LaunchGalleryPicker -> galleryPickerLauncher()
                is DocumentsEffect.OpenDocumentFile -> {
                    // TODO: implement platform-specific file viewing (Android: FileProvider intent; iOS: QuickLook)
                    snackbarHostState.showSnackbar("Download complete: ${effect.fileName}")
                }
            }
        }
    }

    DockifyScaffold(
        topBarConfig = TopBarConfig.Simple(title = "Documents"),
        floatingActionButton = {
            UploadFab(
                isUploading = state.isUploading,
                onClick = { viewModel.onAction(DocumentsAction.UploadFabClicked) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.documents.isEmpty() -> {
                    Text(
                        text = "No documents yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.documents, key = { it.id }) { document ->
                            DocumentItem(
                                document = document,
                                onOpen = { viewModel.onAction(DocumentsAction.OpenDocument(document)) },
                                onDelete = { viewModel.onAction(DocumentsAction.RequestDeleteDocument(document.id)) },
                                enabled = !state.isUploading
                            )
                        }
                    }
                }
            }
        }
    }

    // File picker bottom sheet
    if (state.showFilePicker) {
        FilePickerBottomSheet(
            onPickFromGallery = { viewModel.onAction(DocumentsAction.PickFromGallery) },
            onPickFromFiles = { viewModel.onAction(DocumentsAction.PickFromFiles) },
            onDismiss = { viewModel.onAction(DocumentsAction.FilePickerDismissed) }
        )
    }

    // Delete confirmation dialog
    if (state.pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onAction(DocumentsAction.CancelDeleteDocument) },
            title = { Text("Delete document") },
            text = { Text("This document will be permanently removed. Continue?") },
            confirmButton = {
                TextButton(onClick = { viewModel.onAction(DocumentsAction.ConfirmDeleteDocument) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onAction(DocumentsAction.CancelDeleteDocument) }) {
                    Text("Cancel")
                }
            }
        )
    }
}
