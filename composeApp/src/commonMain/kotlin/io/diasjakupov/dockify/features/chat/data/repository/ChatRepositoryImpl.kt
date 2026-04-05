package io.diasjakupov.dockify.features.chat.data.repository

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.core.domain.map
import io.diasjakupov.dockify.features.chat.data.datasource.ChatRemoteDataSource
import io.diasjakupov.dockify.features.chat.data.mapper.toDomain
import io.diasjakupov.dockify.features.chat.domain.model.ChatMessage
import io.diasjakupov.dockify.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class ChatRepositoryImpl(
    private val remoteDataSource: ChatRemoteDataSource
) : ChatRepository {

    override fun sendMessageStream(userId: Int, docId: String?, message: String): Flow<String> {
        return remoteDataSource.sendMessageStream(userId, docId, message)
    }

    override suspend fun getHistory(userId: Int, docId: String?): Resource<List<ChatMessage>, DataError> {
        return remoteDataSource.getHistory(userId, docId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun clearHistory(userId: Int, docId: String?): EmptyResult<DataError> {
        return remoteDataSource.clearHistory(userId, docId)
    }
}
