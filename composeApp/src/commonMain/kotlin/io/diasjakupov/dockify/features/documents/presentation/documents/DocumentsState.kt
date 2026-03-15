package io.diasjakupov.dockify.features.documents.presentation.documents

import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.ui.base.LoadingState
import io.diasjakupov.dockify.ui.base.UiState
import io.diasjakupov.dockify.ui.base.WithError
import io.diasjakupov.dockify.ui.base.WithLoading

data class DocumentsState(
    val documents: List<Document> = emptyList(),
    /** true only while a file is being uploaded — governs FAB vs progress indicator */
    val isUploading: Boolean = false,
    val showFilePicker: Boolean = false,
    /** ID of the document pending delete confirmation, null if no dialog is showing */
    val pendingDeleteId: String? = null,
    /** Governs list-fetch loading indicator — independent from isUploading */
    override val loadingState: LoadingState = LoadingState.IDLE,
    override val error: String? = null
) : UiState, WithLoading, WithError
