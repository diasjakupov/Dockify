package io.diasjakupov.dockify.features.chat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequestDto(
    @SerialName("user_id") val userId: Int,
    @SerialName("doc_id") val docId: String? = null,
    val message: String
)

@Serializable
data class ChatResponseDto(
    val reply: String
)

@Serializable
data class ChatMessageResponseDto(
    val id: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("doc_id") val docId: String? = null,
    val role: String,
    val content: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class StreamChunkDto(
    val choices: List<StreamChoice> = emptyList()
)

@Serializable
data class StreamChoice(
    val delta: StreamDelta = StreamDelta()
)

@Serializable
data class StreamDelta(
    val content: String = ""
)
