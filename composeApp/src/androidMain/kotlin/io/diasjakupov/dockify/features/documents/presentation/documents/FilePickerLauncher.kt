package io.diasjakupov.dockify.features.documents.presentation.documents

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile

@Composable
actual fun rememberFilePickerLauncher(onResult: (PickedFile?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onResult(uri?.toPickedFile(context))
    }
    return { launcher.launch("*/*") }
}

@Composable
actual fun rememberGalleryPickerLauncher(onResult: (PickedFile?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        onResult(uri?.toPickedFile(context))
    }
    return {
        launcher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
        )
    }
}

private fun Uri.toPickedFile(context: android.content.Context): PickedFile? {
    val bytes = context.contentResolver.openInputStream(this)?.use { it.readBytes() }
        ?: return null
    val mimeType = context.contentResolver.getType(this) ?: "application/octet-stream"
    val fileName = context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        if (nameIndex >= 0) cursor.getString(nameIndex) else "file"
    } ?: "file"
    return PickedFile(fileName = fileName, contentType = mimeType, bytes = bytes)
}
