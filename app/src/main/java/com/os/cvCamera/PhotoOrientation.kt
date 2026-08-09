package com.os.cvCamera

import android.content.ContentResolver
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import timber.log.Timber

/**
 * Writes the EXIF `Orientation` tag into saved photos.
 *
 * The viewfinder is locked to portrait, so the frames OpenCV hands us are always in portrait
 * layout regardless of how the phone is held. A shot taken in landscape therefore lands in the
 * file rotated a quarter turn. Stock camera apps solve this by leaving the pixels alone and
 * recording how the device was held in EXIF; galleries then rotate on display. This does the same.
 *
 * Without the tag a gallery has nothing to go on and shows the raw, sideways pixels.
 */
object PhotoOrientation {
    /**
     * EXIF orientation for a device angle as reported by `OrientationEventListener`
     * (0 = upright, 90 = rotated clockwise a quarter turn, and so on).
     *
     * Rotating the device clockwise makes the scene appear rotated counter-clockwise in our
     * fixed-portrait frame, so the tag asks the viewer to rotate it back clockwise.
     */
    fun exifOrientationFor(deviceAngle: Int): Int =
        when (((deviceAngle % 360) + 360) % 360) {
            90 -> ExifInterface.ORIENTATION_ROTATE_90
            180 -> ExifInterface.ORIENTATION_ROTATE_180
            270 -> ExifInterface.ORIENTATION_ROTATE_270
            else -> ExifInterface.ORIENTATION_NORMAL
        }

    /**
     * Tag an already-written JPEG. Must run after the bytes are flushed, since it rewrites the
     * file's EXIF segment in place.
     */
    fun apply(
        resolver: ContentResolver,
        uri: Uri,
        deviceAngle: Int,
    ) {
        val orientation = exifOrientationFor(deviceAngle)
        if (orientation == ExifInterface.ORIENTATION_NORMAL) return

        try {
            resolver.openFileDescriptor(uri, "rw")?.use { descriptor ->
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
}
