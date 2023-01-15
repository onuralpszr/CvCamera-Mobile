package com.os.cvCamera

import org.junit.Test
import org.junit.Assert.assertEquals
import org.opencv.android.OpenCVLoader.OPENCV_VERSION

/**
 * OpenCV local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
    class CvCheckUnitTest {
    @Test
    fun opencv_version_isCorrect() {
        assertEquals("4.7.0", OPENCV_VERSION)
    }


}