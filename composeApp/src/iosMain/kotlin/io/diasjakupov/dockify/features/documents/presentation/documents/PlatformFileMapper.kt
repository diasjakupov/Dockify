package io.diasjakupov.dockify.features.documents.presentation.documents

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile

actual suspend fun PlatformFile.toPicked(): PickedFile {
    val bytes = readBytes()
    return PickedFile(
        fileName = name,
        contentType = mimeTypeFromExtension(name.substringAfterLast('.', "").lowercase()),
        bytes = bytes
    )
}
