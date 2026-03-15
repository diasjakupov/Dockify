package io.diasjakupov.dockify.features.documents.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PickedFileTest {

    @Test
    fun `two PickedFiles with same bytes are equal`() {
        val bytes = byteArrayOf(1, 2, 3)
        val a = PickedFile("file.pdf", "application/pdf", bytes.copyOf())
        val b = PickedFile("file.pdf", "application/pdf", bytes.copyOf())
        assertEquals(a, b)
    }

    @Test
    fun `two PickedFiles with different bytes are not equal`() {
        val a = PickedFile("file.pdf", "application/pdf", byteArrayOf(1, 2, 3))
        val b = PickedFile("file.pdf", "application/pdf", byteArrayOf(4, 5, 6))
        assertNotEquals(a, b)
    }

    @Test
    fun `two PickedFiles with different names are not equal`() {
        val bytes = byteArrayOf(1, 2, 3)
        val a = PickedFile("a.pdf", "application/pdf", bytes.copyOf())
        val b = PickedFile("b.pdf", "application/pdf", bytes.copyOf())
        assertNotEquals(a, b)
    }
}
