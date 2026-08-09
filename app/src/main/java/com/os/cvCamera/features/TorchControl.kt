package com.os.cvCamera.features

import android.app.Activity
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.TypedValue
import android.view.Menu
import android.widget.Toast
import com.os.cvCamera.ExtendJavaCamera2View
import com.os.cvCamera.R

/**
 * Continuous flash (torch) toggle, backed by [ExtendJavaCamera2View.torchEnabled].
 *
 * Owns the `flashlight` menu item, its icon state and its messaging. The torch is cleared on
 * camera switch because front cameras generally have no flash unit.
 */
class TorchControl(
    private val activity: Activity,
    private val menu: Menu,
    private val cameraView: ExtendJavaCamera2View,
) : CameraFeature {
    override fun attach() = syncIcon()

    override fun onMenuItemSelected(itemId: Int): Boolean {
        if (itemId != R.id.flashlight) return false

        if (!cameraView.hasFlash()) {
            toast(R.string.flashlight_unavailable)
            return true
        }

        cameraView.torchEnabled = !cameraView.torchEnabled
        syncIcon()
        toast(if (cameraView.torchEnabled) R.string.flashlight_on else R.string.flashlight_off)
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

    private fun toast(resId: Int) = Toast.makeText(activity, activity.getString(resId), Toast.LENGTH_SHORT).show()
}
