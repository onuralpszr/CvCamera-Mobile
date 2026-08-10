package com.os.cvcamera.ui.features

import android.app.Activity
import androidx.annotation.StringRes
import com.os.cvcamera.ui.R
import org.opencv.core.Mat
import timber.log.Timber

/**
 * A single frame transformation.
 *
 * Declared as a functional interface so it can be implemented with a lambda from both Kotlin and
 * Java, which lets each example app supply effects written in its own language.
 */
fun interface FrameEffect {
    /**
     * Transform [frame] and return the result. Returning [frame] itself is expected for effects
     * that write in place.
     *
     * Called on the camera thread for every frame.
     */
    fun apply(frame: Mat): Mat
}

/** A [FrameEffect] with a display name. */
class NamedEffect(
    @StringRes val labelRes: Int,
    val effect: FrameEffect,
)

/**
 * Effect selection behind the `filters` menu item, plus the per frame transform.
 *
 * The catalogue is supplied by the app, so each example can demonstrate effects implemented in its
 * own language while sharing this dialog and menu wiring.
 *
 * @param effects offered in order. The first entry is selected initially, so it should be the
 *   no-op effect.
 */
class FilterPicker(
    private val activity: Activity,
    private val effects: List<NamedEffect>,
) : CameraFeature {
    private var selected = 0

    override fun onMenuItemSelected(itemId: Int): Boolean {
        if (itemId != R.id.filters) return false

        val labels = effects.map { activity.getString(it.labelRes) }.toTypedArray()
        activity.showSingleChoiceDialog(R.string.filters, labels, selected) { which ->
            selected = which
            Timber.d("Effect selected: %s", labels[which])
        }
        return true
    }

    override fun processFrame(frame: Mat): Mat = effects[selected].effect.apply(frame)
}
