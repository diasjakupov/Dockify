package io.diasjakupov.dockify.features.documents.presentation.documents

import kotlin.test.Test
import kotlin.test.assertEquals

class MimeTypeTest {
    @Test
    fun pdfExtensionReturnsPdfMime() {
        assertEquals("application/pdf", mimeTypeFromExtension("pdf"))
    }

    @Test
    fun jpegExtensionReturnsJpegMime() {
        assertEquals("image/jpeg", mimeTypeFromExtension("jpeg"))
    }

    @Test
    fun unknownExtensionReturnsOctetStream() {
        assertEquals("application/octet-stream", mimeTypeFromExtension("xyz"))
    }
}
