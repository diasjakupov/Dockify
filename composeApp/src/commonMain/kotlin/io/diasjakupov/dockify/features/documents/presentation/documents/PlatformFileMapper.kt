package io.diasjakupov.dockify.features.documents.presentation.documents

import io.github.vinceglb.filekit.PlatformFile
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile

/**
 * Platform-specific: converts a FileKit [PlatformFile] to the domain [PickedFile].
 * Implemented in androidMain and iosMain where readBytes() is available.
 */
expect suspend fun PlatformFile.toPicked(): PickedFile
