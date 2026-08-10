package com.os.cvcamera.ui.features

import androidx.exifinterface.media.ExifInterface
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the device angle to EXIF orientation mapping. Runs on the host JVM.
 */
class ExifOrientationTest {
    @Test
    fun quadrants_mapToTheMatchingExifTag() {
        assertEquals(ExifOrientation.NORMAL, ExifOrientation.forDeviceAngle(0))
        assertEquals(ExifOrientation.ROTATE_90, ExifOrientation.forDeviceAngle(90))
        assertEquals(ExifOrientation.ROTATE_180, ExifOrientation.forDeviceAngle(180))
        assertEquals(ExifOrientation.ROTATE_270, ExifOrientation.forDeviceAngle(270))
    }

    @Test
    fun constants_matchTheExifSpecification() {
        assertEquals(ExifInterface.ORIENTATION_NORMAL, ExifOrientation.NORMAL)
        assertEquals(ExifInterface.ORIENTATION_ROTATE_90, ExifOrientation.ROTATE_90)
        assertEquals(ExifInterface.ORIENTATION_ROTATE_180, ExifOrientation.ROTATE_180)
        assertEquals(ExifInterface.ORIENTATION_ROTATE_270, ExifOrientation.ROTATE_270)
    }

    @Test
    fun anglesWrapAroundAFullTurn() {
        assertEquals(ExifOrientation.NORMAL, ExifOrientation.forDeviceAngle(360))
        assertEquals(ExifOrientation.ROTATE_90, ExifOrientation.forDeviceAngle(450))
    }

    @Test
    fun negativeAnglesAreNormalised() {
        assertEquals(ExifOrientation.ROTATE_270, ExifOrientation.forDeviceAngle(-90))
        assertEquals(ExifOrientation.ROTATE_180, ExifOrientation.forDeviceAngle(-180))
    }

    @Test
    fun anglesOffTheQuadrantFallBackToNormal() {
        // ControlsRotator snaps before calling, so an unsnapped angle must not produce a
        // half rotated tag.
        assertEquals(ExifOrientation.NORMAL, ExifOrientation.forDeviceAngle(45))
    }
}
