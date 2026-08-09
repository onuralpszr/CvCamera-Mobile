package com.os.cvCamera.features

import android.content.Context
import android.net.Uri
import android.view.OrientationEventListener
import androidx.exifinterface.media.ExifInterface
import timber.log.Timber
import kotlin.math.roundToInt

/**
 * Records how the phone was held into each saved photo's EXIF `Orientation` tag.
 *
 * The viewfinder is locked to portrait, so the frames OpenCV hands us are always in portrait
 * layout regardless of how the phone is held. A shot taken in landscape therefore lands in the
 * file rotated a quarter turn. Stock camera apps leave the pixels alone and record the device
 * angle in EXIF; galleries then rotate on display. Without the tag a gallery has nothing to go on
 * and shows the raw, sideways pixels.
 *
 * Orientation is tracked with a listener of its own rather than shared with [ControlsRotator],
 * so the two features stay independent.
 */
class PhotoOrientation(
    private val context: Context,
) : CameraFeature {
    private var deviceAngle = 0

    private val listener =
        object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                deviceAngle = (orientation / 90.0).roundToInt() * 90 % 360
            }
        }

    override fun onResume() {
        if (listener.canDetectOrientation()) listener.enable()
    }

    override fun onPause() = listener.disable()

    override fun onPhotoSaved(uri: Uri) {
        val orientation = exifOrientationFor(deviceAngle)
        if (orientation == ExifInterface.ORIENTATION_NORMAL) return

        try {
            context.contentResolver.openFileDescriptor(uri, "rw")?.use { descriptor ->
                val exif = ExifInterface(descriptor.fileDescriptor)
                exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
                exif.saveAttributes()
            }
            Timber.d("EXIF orientation $orientation written for device angle $deviceAngle")
        } catch (e: Exception) {
            // A missing tag only affects presentation, so never fail the capture over it.
            Timber.e(e, "Failed to write EXIF orientation")
        }
    }

    private companion object {
        /**
         * EXIF orientation for a device angle as reported by `OrientationEventListener`
         * (0 = upright, 90 = rotated clockwise a quarter turn, and so on).
         *
         * Rotating the device clockwise makes the scene appear rotated counter-clockwise in the
         * fixed-portrait frame, so the tag asks the viewer to rotate it back clockwise.
         */
        fun exifOrientationFor(deviceAngle: Int): Int =
            when (((deviceAngle % 360) + 360) % 360) {
                90 -> ExifInterface.ORIENTATION_ROTATE_90
                180 -> ExifInterface.ORIENTATION_ROTATE_180
                270 -> ExifInterface.ORIENTATION_ROTATE_270
                else -> ExifInterface.ORIENTATION_NORMAL
            }
    }
}
