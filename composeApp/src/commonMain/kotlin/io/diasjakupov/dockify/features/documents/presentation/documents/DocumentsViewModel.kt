package io.diasjakupov.dockify.features.documents.presentation.documents

import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile
import io.diasjakupov.dockify.features.documents.domain.usecase.DeleteDocumentUseCase
import io.diasjakupov.dockify.features.documents.domain.usecase.DownloadDocumentUseCase
import io.diasjakupov.dockify.features.documents.domain.usecase.GetDocumentsUseCase
import io.diasjakupov.dockify.features.documents.domain.usecase.UploadDocumentUseCase
import io.diasjakupov.dockify.ui.base.BaseViewModel
import io.diasjakupov.dockify.ui.base.LoadingState
import io.diasjakupov.dockify.ui.base.toUserMessage

class DocumentsViewModel(
    private val getDocumentsUseCase: GetDocumentsUseCase,
    private val uploadDocumentUseCase: UploadDocumentUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase,
    private val downloadDocumentUseCase: DownloadDocumentUseCase
) : BaseViewModel<DocumentsState, DocumentsAction, DocumentsEffect>(DocumentsState()) {

    override fun handleAction(action: DocumentsAction) {
        when (action) {
            is DocumentsAction.LoadDocuments -> loadDocuments()
            is DocumentsAction.UploadFabClicked -> updateState { copy(showFilePicker = true) }
            is DocumentsAction.FilePickerDismissed -> updateState { copy(showFilePicker = false) }
            is DocumentsAction.PickFromFiles -> {
                updateState { copy(showFilePicker = false) }
                emitEffect(DocumentsEffect.LaunchFilePicker)
            }
            is DocumentsAction.PickFromGallery -> {
                updateState { copy(showFilePicker = false) }
                emitEffect(DocumentsEffect.LaunchGalleryPicker)
            }
            is DocumentsAction.FileSelected -> uploadFile(action.file)
            is DocumentsAction.PickCancelled -> { /* no-op */ }
            is DocumentsAction.RequestDeleteDocument -> {
                updateState { copy(pendingDeleteId = action.id) }
            }
            is DocumentsAction.ConfirmDeleteDocument -> {
                currentState.pendingDeleteId?.let { deleteDocument(it) }
                updateState { copy(pendingDeleteId = null) }
            }
            is DocumentsAction.CancelDeleteDocument -> updateState { copy(pendingDeleteId = null) }
            is DocumentsAction.OpenDocument -> downloadDocument(action.document)
            is DocumentsAction.ErrorDismissed -> updateState { copy(error = null) }
        }
    }

    private fun loadDocuments() {
        updateState { copy(loadingState = LoadingState.LOADING) }
        launch {
            when (val result = getDocumentsUseCase()) {
                is Resource.Success -> updateState {
                    copy(documents = result.data, loadingState = LoadingState.IDLE)
                }
                is Resource.Error -> {
                    val message = result.error.toUserMessage()
                    updateState { copy(loadingState = LoadingState.IDLE, error = message) }
                }
            }
        }
    }

    private fun uploadFile(file: PickedFile) {
        updateState { copy(isUploading = true) }
        launch {
            when (val result = uploadDocumentUseCase(file)) {
                is Resource.Success -> updateState {
                    copy(
                        documents = listOf(result.data) + documents,
                        isUploading = false
                    )
                }
                is Resource.Error -> {
                    val message = result.error.toUserMessage()
                    updateState { copy(isUploading = false, error = message) }
                    emitEffect(DocumentsEffect.ShowSnackbar(message))
                }
            }
        }
    }

    private fun deleteDocument(id: String) {
        launch {
            when (val result = deleteDocumentUseCase(id)) {
                is Resource.Success -> updateState {
                    copy(documents = documents.filter { it.id != id })
                }
                is Resource.Error -> {
                    val message = result.error.toUserMessage()
                    updateState { copy(error = message) }
                    emitEffect(DocumentsEffect.ShowSnackbar(message))
                }
            }
        }
    }

    private fun downloadDocument(document: Document) {
        launch {
            when (val result = downloadDocumentUseCase(document.id)) {
                is Resource.Success -> emitEffect(
                    DocumentsEffect.OpenDocumentFile(result.data, document.fileName, document.contentType)
                )
                is Resource.Error -> {
                    val message = result.error.toUserMessage()
                    emitEffect(DocumentsEffect.ShowSnackbar(message))
                }
            }
        }
    }
}
