package io.diasjakupov.dockify.features.documents.presentation.documents

import androidx.compose.runtime.Composable
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile

@Composable
actual fun rememberFilePickerLauncher(onResult: (PickedFile?) -> Unit): () -> Unit {
    // TODO: implement with UIDocumentPickerViewController when iOS file picker is needed
    return { onResult(null) }
}

@Composable
actual fun rememberGalleryPickerLauncher(onResult: (PickedFile?) -> Unit): () -> Unit {
    // TODO: implement with PHPickerViewController when iOS gallery picker is needed
    return { onResult(null) }
}
