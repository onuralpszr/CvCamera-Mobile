package com.os.cvCamera.features

import android.app.Activity
import android.util.Size
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
            Toast.makeText(activity, activity.getString(R.string.no_resolutions), Toast.LENGTH_SHORT).show()
            return true
        }

        // Frame size is reported in view orientation, supported sizes in sensor orientation.
        val frame = cameraView.getFrameSize()
        val checked =
            sizes.indexOfFirst {
                (it.width == frame.width && it.height == frame.height) ||
                    (it.width == frame.height && it.height == frame.width)
            }

        val labels = sizes.map { describe(it) }.toTypedArray()
        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.cameraResolution)
            .setSingleChoiceItems(labels, checked) { dialog, which ->
                dialog.dismiss()
                if (which == checked) return@setSingleChoiceItems

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
            }.setNegativeButton(android.R.string.cancel, null)
            .show()
        return true
    }

    /** e.g. `1920 × 1080  ·  16:9  ·  2.1 MP` */
    private fun describe(size: Size): String {
        val megaPixels = size.width.toLong() * size.height / 1_000_000.0
        return "${size.width} × ${size.height}  ·  ${aspectRatio(size)}  ·  ${"%.1f".format(megaPixels)} MP"
    }

    private fun aspectRatio(size: Size): String {
        val divisor = gcd(size.width, size.height)
        return "${size.width / divisor}:${size.height / divisor}"
    }

    private tailrec fun gcd(
        a: Int,
        b: Int,
    ): Int = if (b == 0) (if (a == 0) 1 else a) else gcd(b, a % b)
}
