package io.diasjakupov.dockify.features.documents.domain.model

/**
 * An in-memory representation of a file selected by the user before upload.
 * Plain class (not data class) because ByteArray structural equality is broken in data classes.
 */
class PickedFile(
    val fileName: String,
    val contentType: String,
    val bytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedFile) return false
        return fileName == other.fileName &&
               contentType == other.contentType &&
               bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}
