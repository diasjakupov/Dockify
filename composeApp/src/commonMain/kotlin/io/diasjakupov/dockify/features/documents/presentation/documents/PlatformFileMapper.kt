package io.diasjakupov.dockify.features.documents.presentation.documents

import io.github.vinceglb.filekit.PlatformFile
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile

/**
 * Converts a FileKit [PlatformFile] to the domain [PickedFile].
 * Calls [PlatformFile.readBytes] which is a suspend function — must be called from a coroutine.
 * Platform-specific implementations are provided in androidMain and iosMain.
 */
expect suspend fun PlatformFile.toPicked(): PickedFile

/**
 * Maps file extensions to MIME types.
 * Platform-specific implementations are provided in androidMain and iosMain.
 */
internal expect fun mimeTypeFromExtension(ext: String): String
