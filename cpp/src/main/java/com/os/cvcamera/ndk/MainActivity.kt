package com.os.cvcamera.ndk

import com.os.cvcamera.ui.CameraScreenActivity
import com.os.cvcamera.ui.features.AboutDialog
import com.os.cvcamera.ui.features.CameraFeature
import com.os.cvcamera.ui.features.FilterPicker

/**
 * NDK example: the same camera screen as the other examples, with every effect computed in C++.
 *
 * The UI comes from `camera-ui`, so the only difference from the Kotlin example is where the pixels
 * are processed. See `native-lib.cpp` and [NativeEffects].
 */
class MainActivity : CameraScreenActivity() {
    override fun createFeatures(): List<CameraFeature> =
        listOf(
            FilterPicker(this, NativeEffects.all()),
            AboutDialog(
                activity = this,
                versionName = BuildConfig.VERSION_NAME,
                versionCode = BuildConfig.VERSION_CODE,
                implementation = getString(R.string.implementation_ndk),
                openCvNativeVersion = NativeEffects.openCvVersion(),
            ),
        ) + defaultFeatures()
}
