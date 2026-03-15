package io.diasjakupov.dockify.features.documents.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.domain.usecase.GetCurrentUserUseCase
import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.features.documents.domain.repository.DocumentRepository

class GetDocumentsUseCase(
    private val repository: DocumentRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {
    suspend operator fun invoke(): Resource<List<Document>, DataError> {
        val userResult = getCurrentUserUseCase()
        if (userResult is Resource.Error) return userResult

        val userId = (userResult as Resource.Success).data.id.toIntOrNull()
            ?: return Resource.Error(DataError.Auth.UNAUTHORIZED)

        return repository.getDocuments(userId)
    }
}
