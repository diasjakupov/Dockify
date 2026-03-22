package io.diasjakupov.dockify.features.documents.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.diasjakupov.dockify.ui.theme.Navy30

@Composable
fun UploadFab(
    isUploading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = { if (!isUploading) onClick() },
        modifier = modifier,
        containerColor = Navy30,
        contentColor = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Upload file")
            }
        }
    }
}
