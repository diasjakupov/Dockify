package io.diasjakupov.dockify.features.chat.domain.usecase

import io.diasjakupov.dockify.core.domain.DataError
import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.domain.usecase.GetCurrentUserUseCase
import io.diasjakupov.dockify.features.chat.domain.model.ChatMessage
import io.diasjakupov.dockify.features.chat.domain.repository.ChatRepository

class GetChatHistoryUseCase(
    private val repository: ChatRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {
    suspend operator fun invoke(docId: String?): Resource<List<ChatMessage>, DataError> {
        val userResult = getCurrentUserUseCase()
        if (userResult is Resource.Error) return userResult

        val userId = (userResult as Resource.Success).data.id.toIntOrNull()
            ?: return Resource.Error(DataError.Auth.UNAUTHORIZED)

        return repository.getHistory(userId, docId)
    }
}
