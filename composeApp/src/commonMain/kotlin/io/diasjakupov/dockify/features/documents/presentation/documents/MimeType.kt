package io.diasjakupov.dockify.features.documents.presentation.documents

internal fun mimeTypeFromExtension(ext: String): String = when (ext) {
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
