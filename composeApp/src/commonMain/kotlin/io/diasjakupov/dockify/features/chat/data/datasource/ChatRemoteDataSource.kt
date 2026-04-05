package io.diasjakupov.dockify.features.chat.data.datasource

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.EmptyResult
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.chat.data.dto.ChatMessageResponseDto
import kotlinx.coroutines.flow.Flow

interface ChatRemoteDataSource {
    fun sendMessageStream(
        userId: Int,
        docId: String?,
        message: String
    ): Flow<String>

    suspend fun getHistory(
        userId: Int,
        docId: String?
    ): Resource<List<ChatMessageResponseDto>, DataError>

    suspend fun clearHistory(
        userId: Int,
        docId: String?
    ): EmptyResult<DataError>
}
