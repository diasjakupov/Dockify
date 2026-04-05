package io.diasjakupov.dockify.features.chat.domain.repository

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.chat.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun sendMessageStream(userId: Int, docId: String?, message: String): Flow<String>
    suspend fun getHistory(userId: Int, docId: String?): Resource<List<ChatMessage>, DataError>
    suspend fun clearHistory(userId: Int, docId: String?): EmptyResult<DataError>
}
