package io.diasjakupov.dockify.features.documents.data.repository

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.core.domain.map
import io.diasjakupov.dockify.features.documents.data.datasource.DocumentRemoteDataSource
import io.diasjakupov.dockify.features.documents.data.mapper.toDomain
import io.diasjakupov.dockify.features.documents.domain.model.Document
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile
import io.diasjakupov.dockify.features.documents.domain.repository.DocumentRepository

class DocumentRepositoryImpl(
    private val remoteDataSource: DocumentRemoteDataSource
) : DocumentRepository {

    override suspend fun getDocuments(userId: Int): Resource<List<Document>, DataError> {
        return remoteDataSource.getDocuments(userId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun uploadDocument(userId: Int, file: PickedFile): Resource<Document, DataError> {
        return remoteDataSource.uploadDocument(userId, file).map { it.toDomain() }
    }

    override suspend fun deleteDocument(id: String): EmptyResult<DataError> {
        return remoteDataSource.deleteDocument(id)
    }

    override suspend fun downloadDocument(id: String): Resource<ByteArray, DataError> {
        return remoteDataSource.downloadDocument(id)
    }
}
