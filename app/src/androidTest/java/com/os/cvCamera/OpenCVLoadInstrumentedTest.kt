package com.os.cvCamera

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.android.BaseLoaderCallback

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class OpenCVLoadInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.os.cvCamera", appContext.packageName)
    }

    @Test
    fun opencv_isLoaded() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        object : BaseLoaderCallback(appContext) {
            override fun onManagerConnected(status: Int) {
                when (status) {
                    SUCCESS -> {
                        assertEquals(SUCCESS, status)
                    }
                    else -> {
                        assertEquals(SUCCESS, status)
                    }
                }
            }
        }
    }
}
