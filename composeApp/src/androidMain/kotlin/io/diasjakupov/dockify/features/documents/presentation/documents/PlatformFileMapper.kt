package io.diasjakupov.dockify.features.documents.presentation.documents

import io.github.vinceglb.filekit.PlatformFile
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile

/**
 * Android implementation of PlatformFile.toPicked().
 * TODO: Implement once FileKit 0.13.0 exposes PlatformFile's public API
 * The readBytes() and name properties are not accessible through the current API
 */
actual suspend fun PlatformFile.toPicked(): PickedFile {
    error("Not yet implemented - FileKit 0.13.0 API limitation")
}

actual internal fun mimeTypeFromExtension(ext: String): String = when (ext) {
    "pdf"  -> "application/pdf"
    "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    "txt"  -> "text/plain"
    "png"  -> "image/png"
    "jpg", "jpeg" -> "image/jpeg"
    "mp4"  -> "video/mp4"
    "mov"  -> "video/quicktime"
    else   -> "application/octet-stream"
}
