package io.diasjakupov.dockify.features.documents.presentation.documents

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile

// Note: androidMain and iosMain actuals are identical because PlatformFile.readBytes()
// is only available in nonWebMain (not commonMain). This project has no web target,
// so a nonWebMain source set is not configured. Both actuals must be kept in sync.
actual suspend fun PlatformFile.toPicked(): PickedFile {
    val bytes = readBytes()
    return PickedFile(
        fileName = name,
        contentType = mimeTypeFromExtension(name.substringAfterLast('.', "").lowercase()),
        bytes = bytes
    )
}
