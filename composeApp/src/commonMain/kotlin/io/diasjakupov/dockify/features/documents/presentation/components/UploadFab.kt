package io.diasjakupov.dockify.features.documents.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UploadFab(
    isUploading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isUploading) {
        CircularProgressIndicator(
            modifier = modifier.size(56.dp),
            color = MaterialTheme.colorScheme.primary
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Upload file")
        }
    }
}
