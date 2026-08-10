package com.os.cvcamera.ui.features

import android.app.Activity
import android.os.Build
import com.os.cvcamera.ui.R
import org.opencv.android.OpenCVLoader

/**
 * Build and environment details behind the `about` menu item.
 *
 * Version details are supplied by the app rather than read from `BuildConfig`, because this
 * library has no access to the including app's generated fields.
 *
 * @param versionName app version name, normally `BuildConfig.VERSION_NAME`.
 * @param versionCode app version code, normally `BuildConfig.VERSION_CODE`.
 * @param implementation how this app processes frames, for example `OpenCV Java bindings` or
 *   `Native C++`. Distinguishes the example apps from each other.
 * @param openCvNativeVersion version reported by native code, for apps that link OpenCV in C++.
 *   Null hides the line.
 * @param gitHash optional commit the app was built from.
 */
class AboutDialog(
    private val activity: Activity,
    private val versionName: String,
    private val versionCode: Int,
    private val implementation: String,
    private val openCvNativeVersion: String? = null,
    private val gitHash: String? = null,
) : CameraFeature {
    override fun onMenuItemSelected(itemId: Int): Boolean {
        if (itemId != R.id.about) return false

        val body =
            buildString {
                appendLine(activity.getString(R.string.about_app_version, versionName, versionCode))
                gitHash?.let { appendLine(activity.getString(R.string.about_git_hash, it)) }
                appendLine()
                appendLine(activity.getString(R.string.about_implementation, implementation))
                appendLine(activity.getString(R.string.about_opencv, OpenCVLoader.OPENCV_VERSION))
                openCvNativeVersion?.let {
                    appendLine(activity.getString(R.string.about_opencv_native, it))
                }
                appendLine()
                append(activity.getString(R.string.about_android, Build.VERSION.RELEASE, Build.VERSION.SDK_INT))
            }

        activity.showMessageDialog(R.string.about, body)
        return true
    }
}
