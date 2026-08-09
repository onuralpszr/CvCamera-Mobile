package com.os.cvCamera

import android.graphics.Color
import android.media.MediaActionSound
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import timber.log.Timber

/**
 * Classic camera shutter feedback: a quick flash over the preview plus the system shutter sound.
 *
 * Deliberately self-contained so it can be plugged in or pulled out without touching layouts,
 * menus or the capture pipeline:
 *
 * ```
 * private val shutterEffect by lazy { ShutterEffect(binding.root) }  // plug in
 * shutterEffect.play()                                              // at the moment of capture
 * shutterEffect.release()                                           // in onDestroy()
 * ```
 *
 * Deleting those three lines removes the feature completely. Nothing else references it.
 *
 * @param host container the flash overlay is added to; it is created lazily and sits on top.
 * @param withSound play the system `SHUTTER_CLICK` sound alongside the flash.
 * @param flashColor colour of the flash. White reads as a photo being taken; black reads as a
 *   mechanical shutter.
 */
class ShutterEffect(
    private val host: ViewGroup,
    private val withSound: Boolean = true,
    private val flashColor: Int = Color.WHITE,
) {
    private companion object {
        /** Rise is snappy, fall is slower — that asymmetry is what reads as a shutter. */
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

    /** Fire the shutter flash and sound. Safe to call repeatedly; must run on the UI thread. */
    fun play() {
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

    /** Release the sound resources and drop the overlay. Call from `onDestroy()`. */
    fun release() {
        sound?.release()
        overlay?.let { view ->
            view.animate().cancel()
            host.removeView(view)
        }
        overlay = null
    }
}
