package com.os.cvCamera.features

import android.app.Activity
import android.widget.Toast
import com.os.cvCamera.ExtendJavaCamera2View
import com.os.cvCamera.R

/**
 * Shows or hides the FPS/resolution overlay via the `toggleFps` menu item.
 */
class FpsOverlayControl(
    private val activity: Activity,
    private val cameraView: ExtendJavaCamera2View,
) : CameraFeature {
    override fun onMenuItemSelected(itemId: Int): Boolean {
        if (itemId != R.id.toggleFps) return false

        val shown = cameraView.toggleFpsMeter()
        val label = if (shown) R.string.fps_overlay_on else R.string.fps_overlay_off
        Toast.makeText(activity, activity.getString(label), Toast.LENGTH_SHORT).show()
        return true
    }
}
