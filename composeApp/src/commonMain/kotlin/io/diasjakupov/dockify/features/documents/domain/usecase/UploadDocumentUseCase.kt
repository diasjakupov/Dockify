package io.diasjakupov.dockify.features.documents.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.domain.usecase.GetCurrentUserUseCase
import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile
import io.diasjakupov.dockify.features.documents.domain.repository.DocumentRepository

private const val MAX_FILE_SIZE_BYTES: Int = 10 * 1024 * 1024  // 10 MB

class UploadDocumentUseCase(
    private val repository: DocumentRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {
    suspend operator fun invoke(file: PickedFile): Resource<Document, DataError> {
        if (file.bytes.size > MAX_FILE_SIZE_BYTES) {
            return Resource.Error(DataError.Document.FILE_TOO_LARGE)
        }

        val userResult = getCurrentUserUseCase()
        if (userResult is Resource.Error) return userResult

        val userId = (userResult as Resource.Success).data.id.toIntOrNull()
            ?: return Resource.Error(DataError.Auth.UNAUTHORIZED)

        return repository.uploadDocument(userId, file)
    }
}
