package io.diasjakupov.dockify.features.documents.data.mapper

import io.diasjakupov.dockify.features.documents.data.dto.DocumentResponseDto
import kotlin.test.Test
import kotlin.test.assertEquals

class DocumentMapperTest {

    @Test
    fun `toDomain maps all fields correctly`() {
        val dto = DocumentResponseDto(
            id = "abc-123",
            userId = 42,
            fileName = "report.pdf",
            fileSize = 204800L,
            contentType = "application/pdf",
            uploadedAt = "2026-03-13T10:00:00Z"
        )

        val domain = dto.toDomain()

        assertEquals("abc-123", domain.id)
        assertEquals(42, domain.userId)
        assertEquals("report.pdf", domain.fileName)
        assertEquals(204800L, domain.fileSize)
        assertEquals("application/pdf", domain.contentType)
        assertEquals("2026-03-13T10:00:00Z", domain.uploadedAt)
    }
}
