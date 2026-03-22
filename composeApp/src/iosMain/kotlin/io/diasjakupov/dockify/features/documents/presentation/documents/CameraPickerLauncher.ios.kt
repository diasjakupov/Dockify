package io.diasjakupov.dockify.features.documents.presentation.documents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import io.diasjakupov.dockify.features.documents.domain.model.PickedFile
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberCameraPickerLauncher(onResult: (PickedFile?) -> Unit): () -> Unit {
    val currentOnResult by rememberUpdatedState(onResult)
    // Retain both picker and delegate at composition scope to prevent premature GC.
    // UIImagePickerController.delegate is a weak ObjC property; we must keep a strong
    // Kotlin reference to the delegate for the entire presentation lifetime.
    val activePicker = remember { mutableStateOf<UIImagePickerController?>(null) }

    val delegate = remember {
        object : NSObject(), UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                val bytes = image?.let { UIImagePNGRepresentation(it)?.toByteArray() }
                currentOnResult(bytes?.let { PickedFile("camera_photo.png", "image/png", it) })
                picker.dismissViewControllerAnimated(true, null)
                activePicker.value = null
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                currentOnResult(null)
                picker.dismissViewControllerAnimated(true, null)
                activePicker.value = null
            }
        }
    }

    return {
        if (UIImagePickerController.isSourceTypeAvailable(
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            )
        ) {
            // UIImagePickerController is used for camera capture (not photo library).
            // For camera, it remains the recommended API even on iOS 14+.
            val picker = UIImagePickerController().apply {
                sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                this.delegate = delegate
            }
            // Retain picker at composition scope to prevent premature GC before delegate fires.
            activePicker.value = picker

            // Use connectedScenes to find the active root VC (iOS 15+ safe, avoids deprecated keyWindow).
            val rootVC = UIApplication.sharedApplication
                .connectedScenes
                .filterIsInstance<UIWindowScene>()
                .firstOrNull { it.activationState == platform.UIKit.UISceneActivationState.UISceneActivationStateForegroundActive }
                ?.windows
                ?.firstOrNull { it.isKeyWindow }
                ?.rootViewController
                ?: UIApplication.sharedApplication.keyWindow?.rootViewController // iOS 12/13 fallback
            rootVC?.presentViewController(picker, animated = true, completion = null)
        } else {
            currentOnResult(null)
        }
    }
}
