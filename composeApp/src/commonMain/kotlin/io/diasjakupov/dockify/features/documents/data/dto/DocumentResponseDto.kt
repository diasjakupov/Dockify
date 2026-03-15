package io.diasjakupov.dockify.features.documents.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DocumentResponseDto(
    val id: String,
    @SerialName("user_id") val userId: Int,
    @SerialName("file_name") val fileName: String,
    @SerialName("file_size") val fileSize: Long,
    @SerialName("content_type") val contentType: String,
    @SerialName("uploaded_at") val uploadedAt: String
)
