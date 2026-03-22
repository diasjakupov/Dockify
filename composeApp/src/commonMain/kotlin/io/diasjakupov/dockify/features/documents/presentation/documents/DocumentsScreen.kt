package io.diasjakupov.dockify.features.documents.presentation.documents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.features.documents.presentation.components.AddDocumentBottomSheet
import io.diasjakupov.dockify.features.documents.presentation.components.DocumentItem
import io.diasjakupov.dockify.features.documents.presentation.components.UploadFab
import io.diasjakupov.dockify.ui.components.common.DockifyScaffold
import io.diasjakupov.dockify.ui.components.common.TopBarConfig
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DocumentsScreen() {
    val viewModel: DocumentsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // FileKit gallery launcher (images + video)
    val galleryLauncher = rememberFilePickerLauncher(type = FileKitType.ImageAndVideo) { file ->
        if (file != null) {
            coroutineScope.launch {
                viewModel.onAction(DocumentsAction.FileSelected(file.toPicked()))
            }
        } else {
            viewModel.onAction(DocumentsAction.PickCancelled)
        }
    }

    // FileKit file launcher (documents)
    val fileLauncher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = setOf("pdf", "docx", "xlsx", "txt", "png", "jpg", "jpeg"))
    ) { file ->
        if (file != null) {
            coroutineScope.launch {
                viewModel.onAction(DocumentsAction.FileSelected(file.toPicked()))
            }
        } else {
            viewModel.onAction(DocumentsAction.PickCancelled)
        }
    }

    // Camera launcher (expect/actual)
    val cameraLauncher = rememberCameraPickerLauncher { file ->
        if (file != null) viewModel.onAction(DocumentsAction.FileSelected(file))
        else viewModel.onAction(DocumentsAction.PickCancelled)
    }

    LaunchedEffect(Unit) {
        viewModel.onAction(DocumentsAction.LoadDocuments)
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DocumentsEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is DocumentsEffect.LaunchFilePicker -> fileLauncher.launch()
                is DocumentsEffect.LaunchGalleryPicker -> galleryLauncher.launch()
                is DocumentsEffect.LaunchCameraPicker -> cameraLauncher()
                is DocumentsEffect.OpenDocumentFile -> {
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
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No documents yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap + to upload a file from your device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
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

    if (state.showFilePicker) {
        AddDocumentBottomSheet(
            recentDocuments = state.documents,
            onPickFromCamera = { viewModel.onAction(DocumentsAction.PickFromCamera) },
            onPickFromGallery = { viewModel.onAction(DocumentsAction.PickFromGallery) },
            onPickFromFiles = { viewModel.onAction(DocumentsAction.PickFromFiles) },
            onDismiss = { viewModel.onAction(DocumentsAction.FilePickerDismissed) }
        )
    }

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
