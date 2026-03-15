package io.diasjakupov.dockify.features.documents.domain.repository

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile

interface DocumentRepository {
    suspend fun getDocuments(userId: Int): Resource<List<Document>, DataError>
    suspend fun uploadDocument(userId: Int, file: PickedFile): Resource<Document, DataError>
    suspend fun deleteDocument(id: String): EmptyResult<DataError>
    suspend fun downloadDocument(id: String): Resource<ByteArray, DataError>
}
