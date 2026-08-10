package com.os.cvCamera.features

import android.app.Activity
import android.widget.Toast
import com.os.cvCamera.ExtendJavaCamera2View
import com.os.cvCamera.R

/**
 * Camera resolution picker behind the `cameraResolution` menu item.
 *
 * Lists the supported frame sizes largest first, labelled with aspect ratio and megapixels, with
 * the size currently in use pre-selected. Changing the size restarts the camera; selecting the
 * current size does nothing.
 */
class ResolutionPicker(
    private val activity: Activity,
    private val cameraView: ExtendJavaCamera2View,
) : CameraFeature {
    override fun onMenuItemSelected(itemId: Int): Boolean {
        if (itemId != R.id.cameraResolution) return false

        val sizes =
            cameraView
                .getSupportedPreviewSizes()
                .distinct()
                .sortedByDescending { it.width.toLong() * it.height }

        if (sizes.isEmpty()) {
            activity.showToast(R.string.no_resolutions)
            return true
        }

        // Frame size is reported in view orientation, supported sizes in sensor orientation.
        val frame = cameraView.getFrameSize()
        val checked =
            sizes.indexOfFirst {
                (it.width == frame.width && it.height == frame.height) ||
                    (it.width == frame.height && it.height == frame.width)
            }

        activity.showSingleChoiceDialog(
            R.string.cameraResolution,
            sizes
                .map {
                    SizeLabel.describe(it.width, it.height)
                }.toTypedArray(),
            checked,
        ) { which ->
            val selected = sizes[which]
            Toast
                .makeText(
                    activity,
                    activity.getString(R.string.switching_resolution, selected.width, selected.height),
                    Toast.LENGTH_SHORT,
                ).show()
            // Let the dialog finish dismissing before the camera restart so the reconnect
            // does not stutter the dismiss animation.
            cameraView.post {
                cameraView.disableView()
                cameraView.setCameraResolution(selected.width, selected.height)
                cameraView.enableView()
            }
        }
        return true
    }
}
