package io.diasjakupov.dockify.features.chat.domain.model

data class ChatMessage(
    val id: Int,
    val userId: Int,
    val docId: String?,
    val role: String,
    val content: String,
    val createdAt: String
)
