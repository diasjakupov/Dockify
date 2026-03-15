package io.diasjakupov.dockify.features.documents.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.documents.data.dto.DocumentResponseDto
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile

interface DocumentRemoteDataSource {
    suspend fun getDocuments(userId: Int): Resource<List<DocumentResponseDto>, DataError>
    suspend fun uploadDocument(userId: Int, file: PickedFile): Resource<DocumentResponseDto, DataError>
    suspend fun deleteDocument(id: String): EmptyResult<DataError>
    suspend fun downloadDocument(id: String): Resource<ByteArray, DataError>
}
