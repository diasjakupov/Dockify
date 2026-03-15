package io.diasjakupov.dockify.features.documents.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.features.documents.domain.repository.DocumentRepository

class DeleteDocumentUseCase(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(id: String): EmptyResult<DataError> {
        return repository.deleteDocument(id)
    }
}
