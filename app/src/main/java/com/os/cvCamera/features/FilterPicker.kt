package com.os.cvCamera.features

import android.app.Activity
import com.os.cvCamera.R
import com.os.cvCamera.toBinary
import com.os.cvCamera.toBlur
import com.os.cvCamera.toCanny
import com.os.cvCamera.toCartoon
import com.os.cvCamera.toContours
import com.os.cvCamera.toEdgeDetection
import com.os.cvCamera.toEmboss
import com.os.cvCamera.toGray
import com.os.cvCamera.toHSV
import com.os.cvCamera.toNegative
import com.os.cvCamera.toPosterize
import com.os.cvCamera.toSepia
import com.os.cvCamera.toSharpen
import com.os.cvCamera.toSketch
import com.os.cvCamera.toSobel
import com.os.cvCamera.toVignette
import org.opencv.core.Mat
import timber.log.Timber

/**
 * Image effect selection and application.
 *
 * Owns the effect catalogue, the picker dialog behind the `filters` menu item, and the per-frame
 * transform. Effects are implemented as `Mat` extensions in `CvFilters.kt`.
 */
class FilterPicker(
    private val activity: Activity,
) : CameraFeature {
    /**
     * Selectable effects in display order. "No effect" is first so it reads as the default, and
     * its transform is the identity.
     */
    private val effects: List<Effect> =
        listOf(
            Effect(R.string.no_filter) { it },
            Effect(R.string.grayscale_filter) { it.toGray() },
            Effect(R.string.sepia_filter) { it.toSepia() },
            Effect(R.string.blur_filter) { it.toBlur() },
            Effect(R.string.hsv_filter) { it.toHSV() },
            Effect(R.string.edge_filter) { it.toEdgeDetection() },
            Effect(R.string.sobel_filter) { it.toSobel() },
            Effect(R.string.canny_filter) { it.toCanny() },
            Effect(R.string.negative_filter) { it.toNegative() },
            Effect(R.string.sharpen_filter) { it.toSharpen() },
            Effect(R.string.emboss_filter) { it.toEmboss() },
            Effect(R.string.cartoon_filter) { it.toCartoon() },
            Effect(R.string.binary_filter) { it.toBinary() },
            Effect(R.string.sketch_filter) { it.toSketch() },
            Effect(R.string.contours_filter) { it.toContours() },
            Effect(R.string.posterize_filter) { it.toPosterize() },
            Effect(R.string.vignette_filter) { it.toVignette() },
        )

    /** Index into [effects]; 0 is "No effect". */
    private var selected = 0

    override fun onMenuItemSelected(itemId: Int): Boolean {
        if (itemId != R.id.filters) return false

        val labels = effects.map { activity.getString(it.labelRes) }.toTypedArray()
        activity.showSingleChoiceDialog(R.string.filters, labels, selected) { which ->
            selected = which
            Timber.d("Effect selected: ${labels[which]}")
        }
        return true
    }

    override fun processFrame(frame: Mat): Mat = effects[selected].apply(frame)

    private class Effect(
        val labelRes: Int,
        val apply: (Mat) -> Mat,
    )
}
