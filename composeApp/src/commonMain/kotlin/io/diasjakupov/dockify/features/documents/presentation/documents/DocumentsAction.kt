package io.diasjakupov.dockify.features.documents.presentation.documents

import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile
import io.diasjakupov.dockify.ui.base.UiAction

sealed interface DocumentsAction : UiAction {
    /** Dispatched via LaunchedEffect(Unit) when the screen is first composed */
    data object LoadDocuments : DocumentsAction
    /** Opens the picker type chooser bottom sheet */
    data object UploadFabClicked : DocumentsAction
    /** Launches gallery picker (effect sent to screen) */
    data object PickFromGallery : DocumentsAction
    /** Launches file picker (effect sent to screen) */
    data object PickFromFiles : DocumentsAction
    /** Launches camera picker (effect sent to screen) */
    data object PickFromCamera : DocumentsAction
    /** Screen delivers picked file after launcher returns */
    data class FileSelected(val file: PickedFile) : DocumentsAction
    /** Screen reports picker cancelled */
    data object PickCancelled : DocumentsAction
    /** User taps delete icon — shows confirmation dialog */
    data class RequestDeleteDocument(val id: String) : DocumentsAction
    /** User confirms delete in dialog */
    data object ConfirmDeleteDocument : DocumentsAction
    /** User cancels delete dialog */
    data object CancelDeleteDocument : DocumentsAction
    /** Tap on document row — triggers download via GET /api/v1/documents/:id/download */
    data class OpenDocument(val document: Document) : DocumentsAction
    data object ErrorDismissed : DocumentsAction
    data object FilePickerDismissed : DocumentsAction
}
