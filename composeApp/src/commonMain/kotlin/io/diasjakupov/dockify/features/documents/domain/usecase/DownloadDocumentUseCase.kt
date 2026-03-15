package io.diasjakupov.dockify.features.documents.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.documents.domain.repository.DocumentRepository

class DownloadDocumentUseCase(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(id: String): Resource<ByteArray, DataError> {
        return repository.downloadDocument(id)
    }
}
