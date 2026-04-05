package io.diasjakupov.dockify.features.documents.domain.model

data class Document(
    val id: String,
    val userId: Int,
    val fileName: String,
    val fileSize: Long,
    val contentType: String,
    val summary: String,
    val uploadedAt: String
)
