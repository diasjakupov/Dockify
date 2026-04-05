package io.diasjakupov.dockify.features.chat.data.mapper

import io.diasjakupov.dockify.features.chat.data.dto.ChatMessageResponseDto
import io.diasjakupov.dockify.features.chat.domain.model.ChatMessage

fun ChatMessageResponseDto.toDomain(): ChatMessage = ChatMessage(
    id = id,
    userId = userId,
    docId = docId,
    role = role,
    content = content,
    createdAt = createdAt
)
