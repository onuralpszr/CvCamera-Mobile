package com.os.cvCamera

import android.os.Bundle
import com.os.cvcamera.ui.CameraScreenActivity
import com.os.cvcamera.ui.features.AboutDialog
import com.os.cvcamera.ui.features.CameraFeature
import com.os.cvcamera.ui.features.FilterPicker

/**
 * Kotlin example: the whole pipeline runs through the OpenCV Java bindings, with no native code in
 * this app.
 *
 * The camera screen, bottom bar and generic features come from `camera-ui`. Only the parts specific
 * to this implementation live here: the effect catalogue in [KotlinEffects] and Haar cascade face
 * detection.
 */
class MainActivity : CameraScreenActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Must happen before the preview is inflated.
        System.loadLibrary("opencv_java4")
        super.onCreate(savedInstanceState)
    }

    override fun createFeatures(): List<CameraFeature> =
        listOf(
            FilterPicker(this, KotlinEffects.all()),
            FaceDetection(this),
            AboutDialog(
                activity = this,
                versionName = BuildConfig.VERSION_NAME,
                versionCode = BuildConfig.VERSION_CODE,
                implementation = getString(R.string.implementation_kotlin),
                gitHash = BuildConfig.GIT_HASH,
            ),
        ) + defaultFeatures()
}
