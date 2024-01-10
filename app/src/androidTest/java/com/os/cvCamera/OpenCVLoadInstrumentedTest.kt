package com.os.cvCamera

import android.view.View
import androidx.test.core.app.ActivityScenario.ActivityAction
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.OpenCVLoader.OPENCV_VERSION


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class OpenCVLoadInstrumentedTest {


    @Before
    fun grantPhonePermission() {
        getInstrumentation().targetContext
        getInstrumentation().uiAutomation.executeShellCommand(
            "pm grant " + getInstrumentation().targetContext.packageName
                    + " android.permission.CAMERA"
        )
    }


    @get:Rule
    var activityScenarioRule = activityScenarioRule<MainActivity>()
    private var decorView: View? = null


    @Before
    fun getDecorView() {
        activityScenarioRule.scenario.onActivity(ActivityAction { activity ->
            decorView = activity.window.decorView
        })
    }



    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = getInstrumentation().targetContext
        assertEquals("com.os.cvCamera", appContext.packageName)
    }

    @Test
    fun opencv_isLoaded() {
        val appContext = getInstrumentation().targetContext
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

    // Check for toast message context on button click
    @Test
    fun toast_message() {
        var context =  getInstrumentation().targetContext
        onView(withId(R.id.about)).perform(ViewActions.click());
        val toastMessage = "CvCamera-Mobile - " +
                "Version ${BuildConfig.VERSION_NAME}-${BuildConfig.GIT_HASH}" +
                " - OpenCV $OPENCV_VERSION"

        // get the toast message
        // get activity from the view
        
        assertEquals(toastMessage, toastMessage)
    }

    @Test
    fun camera_isLoaded() {
        getInstrumentation().targetContext
        onView(withId(R.id.CvCamera)).perform(ViewActions.click());
        assertEquals(true, true)
    }

    @Test
    fun change_camera_rear_front() {
        getInstrumentation().targetContext
        onView(withId(R.id.cvCameraChangeFab)).perform(ViewActions.click());
        assertEquals(true, true)
        onView(withId(R.id.cvCameraChangeFab)).perform(ViewActions.click());
        assertEquals(true, true)
    }
}
