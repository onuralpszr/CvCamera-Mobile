package com.os.cvCamera

import android.content.Context
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Stock-camera-app rotation behaviour: the controls stay pinned to the physical bottom of the
 * screen and only their icons spin to stay upright.
 *
 * This relies on the activity being locked to portrait (see `screenOrientation` in the manifest),
 * so the layout never reflows; the device angle is tracked separately and applied as a view
 * rotation.
 *
 * Self-contained, like [ShutterEffect] — plug it in with three lines:
 * ```
 * private val controlsRotator by lazy { ControlsRotator(this) { rotatableControls() } }
 * controlsRotator.start()   // onResume
 * controlsRotator.stop()    // onPause
 * ```
 *
 * @param context used to observe device orientation.
 * @param targets evaluated on each change, so views created later (menu items are inflated
 *   asynchronously) are still picked up.
 */
class ControlsRotator(
    context: Context,
    private val targets: () -> List<View>,
) {
    private companion object {
        const val ANIMATION_MS = 220L

        /** Ignore jitter below this many degrees from the current snapped angle. */
        const val SNAP_SLOP = 20
    }

    /** Device angle snapped to a quadrant: 0, 90, 180 or 270. */
    private var currentAngle = 0

    private val listener =
        object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                val snapped = snapToQuadrant(orientation)
                if (snapped == currentAngle) return
                // Only commit once the device is clearly past the boundary, so icons do not
                // flap back and forth when held near 45 degrees.
                if (angularDistance(orientation, currentAngle) < 90 - SNAP_SLOP) return

                currentAngle = snapped
                applyRotation(snapped)
            }
        }

    fun start() {
        if (listener.canDetectOrientation()) {
            listener.enable()
        } else {
            Timber.w("Device cannot detect orientation, control icons will not rotate")
        }
    }

    fun stop() = listener.disable()

    private fun snapToQuadrant(orientation: Int) = (orientation / 90.0).roundToInt() * 90 % 360

    /** Smallest absolute difference between two angles, accounting for wrap-around. */
    private fun angularDistance(
        a: Int,
        b: Int,
    ): Int {
        val diff = abs(a - b) % 360
        return if (diff > 180) 360 - diff else diff
    }

    private fun applyRotation(deviceAngle: Int) {
        // Counter-rotate so the icon stays upright with respect to the world.
        val target = -deviceAngle.toFloat()
        for (view in targets()) {
            // Animate along the shortest arc rather than spinning the long way round.
            var delta = (target - view.rotation) % 360f
            if (delta > 180f) delta -= 360f
            if (delta < -180f) delta += 360f

            view
                .animate()
                .rotation(view.rotation + delta)
                .setDuration(ANIMATION_MS)
                .start()
        }
    }
}

/** Every leaf view under [this], used to reach the icons inside a menu container. */
fun ViewGroup.leafViews(): List<View> {
    val leaves = mutableListOf<View>()
    for (index in 0 until childCount) {
        when (val child = getChildAt(index)) {
            is ViewGroup -> leaves += child.leafViews()
            else -> leaves += child
        }
    }
    return leaves
}
