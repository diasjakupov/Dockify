package io.diasjakupov.dockify.features.documents.data.mapper

import io.diasjakupov.dockify.features.documents.data.dto.DocumentResponseDto
import io.diasjakupov.dockify.features.documents.domain.model.Document

fun DocumentResponseDto.toDomain(): Document = Document(
    id = id,
    userId = userId,
    fileName = fileName,
    fileSize = fileSize,
    contentType = contentType,
    summary = summary,
    uploadedAt = uploadedAt
)
