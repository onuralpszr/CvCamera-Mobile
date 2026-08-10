package com.os.cvcamera.ui.features

import android.app.Activity
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.TypedValue
import android.view.Menu
import com.os.cvcamera.ui.CvCameraView
import com.os.cvcamera.ui.R

/**
 * Continuous flash (torch) toggle, backed by [CvCameraView.torchEnabled].
 *
 * Owns the `flashlight` menu item, its icon state and its messaging. The torch is cleared on
 * camera switch because front cameras generally have no flash unit.
 */
class TorchControl(
    private val activity: Activity,
    private val menu: Menu,
    private val cameraView: CvCameraView,
) : CameraFeature {
    override fun attach() = syncIcon()

    override fun onMenuItemSelected(itemId: Int): Boolean {
        if (itemId != R.id.flashlight) return false

        if (!cameraView.hasFlash()) {
            activity.showToast(R.string.flashlight_unavailable)
            return true
        }

        cameraView.torchEnabled = !cameraView.torchEnabled
        syncIcon()
        activity.showToast(if (cameraView.torchEnabled) R.string.flashlight_on else R.string.flashlight_off)
        return true
    }

    /** The target camera may have no flash, so drop the torch and refresh the icon. */
    override fun onCameraSwitched() {
        cameraView.torchEnabled = false
        syncIcon()
    }

    private fun syncIcon() {
        val item = menu.findItem(R.id.flashlight) ?: return
        item.setIcon(
            if (cameraView.torchEnabled) {
                R.drawable.baseline_flashlight_on_24
            } else {
                R.drawable.baseline_flashlight_off_24
            },
        )
        val accent = TypedValue()
        activity.theme.resolveAttribute(android.R.attr.colorAccent, accent, true)
        item.icon?.colorFilter = PorterDuffColorFilter(accent.data, PorterDuff.Mode.SRC_ATOP)
    }
}
