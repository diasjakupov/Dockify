package io.diasjakupov.dockify.features.chat.domain.usecase

import io.diasjakupov.dockify.core.domain.Resource
import io.diasjakupov.dockify.features.auth.domain.usecase.GetCurrentUserUseCase
import io.diasjakupov.dockify.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class SendMessageUseCase(
    private val repository: ChatRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {
    suspend operator fun invoke(docId: String?, message: String): Flow<String> {
        val userResult = getCurrentUserUseCase()
        if (userResult is Resource.Error) return emptyFlow()

        val userId = (userResult as Resource.Success).data.id.toIntOrNull()
            ?: return emptyFlow()

        return repository.sendMessageStream(userId, docId, message)
    }
}
