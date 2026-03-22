package io.diasjakupov.dockify.features.documents.presentation.documents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import platform.UIKit.UIImagePNGRepresentation

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberCameraPickerLauncher(onResult: (PickedFile?) -> Unit): () -> Unit {
    val delegate = remember {
        object : NSObject(), UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image = didFinishPickingMediaWithInfo[
                    platform.UIKit.UIImagePickerControllerOriginalImage
                ] as? platform.UIKit.UIImage
                val bytes = image?.let { UIImagePNGRepresentation(it)?.toByteArray() }
                onResult(bytes?.let { PickedFile("camera_photo.png", "image/png", it) })
                picker.dismissViewControllerAnimated(true, null)
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                onResult(null)
                picker.dismissViewControllerAnimated(true, null)
            }
        }
    }

    return {
        if (UIImagePickerController.isSourceTypeAvailable(
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            )
        ) {
            val picker = UIImagePickerController().apply {
                sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                this.delegate = delegate
            }
            // Use connectedScenes to find the active root VC — avoids deprecated keyWindow on iOS 15+
            val rootVC = UIApplication.sharedApplication
                .connectedScenes
                .filterIsInstance<platform.UIKit.UIWindowScene>()
                .firstOrNull { it.activationState == platform.UIKit.UISceneActivationState.UISceneActivationStateForegroundActive }
                ?.windows
                ?.firstOrNull { it.isKeyWindow }
                ?.rootViewController
                ?: UIApplication.sharedApplication.keyWindow?.rootViewController // iOS 12 fallback
            rootVC?.presentViewController(picker, animated = true, completion = null)
        } else {
            onResult(null)
        }
    }
}
