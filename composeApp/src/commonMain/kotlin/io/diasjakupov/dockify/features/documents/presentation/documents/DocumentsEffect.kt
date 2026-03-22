package io.diasjakupov.dockify.features.documents.presentation.documents

import io.diasjakupov.dockify.ui.base.UiEffect

sealed interface DocumentsEffect : UiEffect {
    data class ShowSnackbar(val message: String) : DocumentsEffect
    data object LaunchFilePicker : DocumentsEffect
    data object LaunchGalleryPicker : DocumentsEffect
    data object LaunchCameraPicker : DocumentsEffect
    data class OpenDocumentFile(
        val bytes: ByteArray,
        val fileName: String,
        val contentType: String
    ) : DocumentsEffect
}
