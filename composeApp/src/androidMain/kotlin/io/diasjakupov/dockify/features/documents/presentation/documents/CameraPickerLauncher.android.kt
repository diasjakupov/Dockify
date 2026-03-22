package io.diasjakupov.dockify.features.documents.presentation.documents

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile
import java.io.File

@Composable
actual fun rememberCameraPickerLauncher(onResult: (PickedFile?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val tempUri = remember { createTempImageUri(context) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val bytes = context.contentResolver.openInputStream(tempUri)?.use { it.readBytes() }
            onResult(bytes?.let { PickedFile("camera_photo.jpg", "image/jpeg", it) })
        } else {
            onResult(null)
        }
    }
    return { launcher.launch(tempUri) }
}

private fun createTempImageUri(context: Context): Uri {
    val tmpFile = File.createTempFile("camera_capture", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tmpFile
    )
}
