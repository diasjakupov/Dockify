package io.diasjakupov.dockify.features.documents.presentation.documents

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile
import java.io.File

@Composable
actual fun rememberCameraPickerLauncher(onResult: (PickedFile?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)
    // Holds the URI created for the current (or most recent) launch so the result
    // callback can read it. A new URI is generated on each launch invocation.
    val pendingUri = remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val bytes = pendingUri.value?.let { uri ->
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
            currentOnResult(bytes?.let { PickedFile("camera_photo.jpg", "image/jpeg", it) })
        } else {
            currentOnResult(null)
        }
        pendingUri.value = null
    }

    return {
        val uri = createTempImageUri(context)
        pendingUri.value = uri
        launcher.launch(uri)
    }
}

private fun createTempImageUri(context: Context): Uri {
    val tmpFile = File.createTempFile("camera_capture_${System.currentTimeMillis()}", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tmpFile
    )
}
