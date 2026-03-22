package io.diasjakupov.dockify.features.documents.presentation.documents

import androidx.compose.runtime.Composable
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile

/**
 * Returns a lambda that launches the native camera when invoked.
 * [onResult] is called with the captured [PickedFile] or null if cancelled.
 */
@Composable
expect fun rememberCameraPickerLauncher(onResult: (PickedFile?) -> Unit): () -> Unit
