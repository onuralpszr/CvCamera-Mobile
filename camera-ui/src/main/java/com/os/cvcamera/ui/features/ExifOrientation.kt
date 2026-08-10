package com.os.cvcamera.ui.features

/**
 * Maps a device rotation to the EXIF `Orientation` tag value written into saved photos.
 *
 * The values match the EXIF specification and `ExifInterface.ORIENTATION_*`, but are kept as plain
 * integers so the mapping stays free of the Android framework and can be unit tested on the host
 * JVM.
 */
internal object ExifOrientation {
    const val NORMAL = 1
    const val ROTATE_180 = 3
    const val ROTATE_90 = 6
    const val ROTATE_270 = 8

    /**
     * EXIF orientation for a device angle as reported by `OrientationEventListener`, where 0 is
     * upright and 90 is a quarter turn clockwise.
     *
     * The viewfinder is locked to portrait, so rotating the device clockwise makes the scene appear
     * rotated counter clockwise in the frame. The tag therefore asks the viewer to rotate it back
     * clockwise. Angles that are not a multiple of 90 are snapped to the nearest quadrant.
     */
    fun forDeviceAngle(angle: Int): Int =
        when (((angle % 360) + 360) % 360) {
            90 -> ROTATE_90
            180 -> ROTATE_180
            270 -> ROTATE_270
            else -> NORMAL
        }
}
