package com.os.cvCamera.features

import android.app.Activity
import android.widget.Toast
import com.os.cvCamera.CanvasScaleMode
import com.os.cvCamera.ExtendJavaCamera2View
import com.os.cvCamera.R

/**
 * Switches the preview between [CanvasScaleMode.FIT] (whole frame, letterboxed) and
 * [CanvasScaleMode.FILL] (centre-cropped), via the `resizeCanvas` menu item.
 *
 * OpenCV re-reads its scale factor for every drawn frame, so a change takes effect on the next
 * frame and the camera keeps running.
 */
class CanvasScaleControl(
    private val activity: Activity,
    private val cameraView: ExtendJavaCamera2View,
) : CameraFeature {
    override fun onMenuItemSelected(itemId: Int): Boolean {
        if (itemId != R.id.resizeCanvas) return false

        val next =
            if (cameraView.canvasScaleMode == CanvasScaleMode.FIT) {
                CanvasScaleMode.FILL
            } else {
                CanvasScaleMode.FIT
            }
        cameraView.canvasScaleMode = next

        val label = if (next == CanvasScaleMode.FILL) R.string.canvas_fill else R.string.canvas_fit
        Toast.makeText(activity, activity.getString(label), Toast.LENGTH_SHORT).show()
        return true
    }
}
