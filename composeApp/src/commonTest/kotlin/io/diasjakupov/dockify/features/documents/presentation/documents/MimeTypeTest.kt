package io.diasjakupov.dockify.features.documents.presentation.documents

import kotlin.test.Test
import kotlin.test.assertEquals

class MimeTypeTest {
    @Test fun pdfExtensionReturnsPdfMime() =
        assertEquals("application/pdf", mimeTypeFromExtension("pdf"))

    @Test fun docxExtensionReturnsWordMime() =
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", mimeTypeFromExtension("docx"))

    @Test fun xlsxExtensionReturnsExcelMime() =
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", mimeTypeFromExtension("xlsx"))

    @Test fun txtExtensionReturnsPlainTextMime() =
        assertEquals("text/plain", mimeTypeFromExtension("txt"))

    @Test fun pngExtensionReturnsPngMime() =
        assertEquals("image/png", mimeTypeFromExtension("png"))

    @Test fun jpgExtensionReturnsJpegMime() =
        assertEquals("image/jpeg", mimeTypeFromExtension("jpg"))

    @Test fun jpegExtensionReturnsJpegMime() =
        assertEquals("image/jpeg", mimeTypeFromExtension("jpeg"))

    @Test fun mp4ExtensionReturnsMp4Mime() =
        assertEquals("video/mp4", mimeTypeFromExtension("mp4"))

    @Test fun movExtensionReturnsQuicktimeMime() =
        assertEquals("video/quicktime", mimeTypeFromExtension("mov"))

    @Test fun unknownExtensionReturnsOctetStream() =
        assertEquals("application/octet-stream", mimeTypeFromExtension("xyz"))
}
