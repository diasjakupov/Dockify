package io.diasjakupov.dockify.features.documents.presentation.documents

import androidx.compose.runtime.Composable
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile

/**
 * Returns a lambda that launches the native file picker when invoked.
 * [onResult] is called with the selected [PickedFile] or null if cancelled.
 */
@Composable
expect fun rememberFilePickerLauncher(onResult: (PickedFile?) -> Unit): () -> Unit

/**
 * Returns a lambda that launches the native gallery picker when invoked.
 * [onResult] is called with the selected [PickedFile] or null if cancelled.
 */
@Composable
expect fun rememberGalleryPickerLauncher(onResult: (PickedFile?) -> Unit): () -> Unit
