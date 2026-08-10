package com.os.cvcamera.ui.features

import android.graphics.Color
import android.media.MediaActionSound
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import timber.log.Timber

/**
 * Classic camera shutter feedback: a quick flash over the preview plus the system shutter sound.
 *
 * Triggered by [CameraFeature.onPhotoCaptureStarted], so the flash coincides with the frame that
 * is saved. The overlay view is created lazily and added to [host]; no layout changes are needed.
 *
 * @param host container the flash overlay is added to.
 * @param withSound whether the system `SHUTTER_CLICK` sound accompanies the flash.
 * @param flashColor colour of the flash overlay.
 */
class ShutterEffect(
    private val host: ViewGroup,
    private val withSound: Boolean = true,
    private val flashColor: Int = Color.WHITE,
) : CameraFeature {
    private companion object {
        /** Rise is snappy and fall is slower. That asymmetry is what reads as a shutter. */
        const val FADE_IN_MS = 45L
        const val FADE_OUT_MS = 190L
        const val PEAK_ALPHA = 0.85f
    }

    private var overlay: View? = null

    private val sound: MediaActionSound? =
        if (withSound) {
            runCatching {
                MediaActionSound().apply {
                    // Preload so the first capture is not delayed by lazy decoding.
                    load(MediaActionSound.SHUTTER_CLICK)
                }
            }.onFailure { Timber.w(it, "Shutter sound unavailable") }.getOrNull()
        } else {
            null
        }

    private fun ensureOverlay(): View {
        overlay?.let { return it }
        val view =
            View(host.context).apply {
                layoutParams =
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                setBackgroundColor(flashColor)
                alpha = 0f
                // Never steal touches from the camera UI underneath.
                isClickable = false
                isFocusable = false
                elevation = Float.MAX_VALUE
            }
        host.addView(view)
        overlay = view
        return view
    }

    /** Plays the flash and shutter sound. Safe to call from any thread. */
    override fun onPhotoCaptureStarted() {
        host.post {
            sound?.play(MediaActionSound.SHUTTER_CLICK)

            val view = ensureOverlay()
            view.bringToFront()
            view.animate().cancel()
            view.alpha = 0f
            view
                .animate()
                .alpha(PEAK_ALPHA)
                .setDuration(FADE_IN_MS)
                .withEndAction {
                    view
                        .animate()
                        .alpha(0f)
                        .setDuration(FADE_OUT_MS)
                        .start()
                }.start()
        }
    }

    /** Releases the sound resources and removes the overlay. */
    override fun detach() {
        sound?.release()
        overlay?.let { view ->
            view.animate().cancel()
            host.removeView(view)
        }
        overlay = null
    }
}
