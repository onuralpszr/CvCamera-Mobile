package com.os.cvCamera

import com.os.cvcamera.ui.features.NamedEffect
import com.os.cvcamera.ui.R as UiR

/**
 * Effect catalogue for the Kotlin example, implemented with the OpenCV Java bindings through the
 * `Mat` extension functions in [CvFilters].
 *
 * The C++ example builds the same list from native functions instead, which is the difference the
 * examples exist to show.
 */
object KotlinEffects {
    fun all(): List<NamedEffect> =
        listOf(
            NamedEffect(UiR.string.effect_none) { it },
            NamedEffect(UiR.string.effect_greyscale) { it.toGray() },
            NamedEffect(UiR.string.effect_sepia) { it.toSepia() },
            NamedEffect(UiR.string.effect_blur) { it.toBlur() },
            NamedEffect(UiR.string.effect_hsv) { it.toHSV() },
            NamedEffect(UiR.string.effect_edge) { it.toEdgeDetection() },
            NamedEffect(UiR.string.effect_sobel) { it.toSobel() },
            NamedEffect(UiR.string.effect_canny) { it.toCanny() },
            NamedEffect(UiR.string.effect_negative) { it.toNegative() },
            NamedEffect(UiR.string.effect_sharpen) { it.toSharpen() },
            NamedEffect(UiR.string.effect_emboss) { it.toEmboss() },
            NamedEffect(UiR.string.effect_cartoon) { it.toCartoon() },
            NamedEffect(UiR.string.effect_binary) { it.toBinary() },
            NamedEffect(UiR.string.effect_sketch) { it.toSketch() },
            NamedEffect(UiR.string.effect_contours) { it.toContours() },
            NamedEffect(UiR.string.effect_posterize) { it.toPosterize() },
            NamedEffect(UiR.string.effect_vignette) { it.toVignette() },
        )
}
