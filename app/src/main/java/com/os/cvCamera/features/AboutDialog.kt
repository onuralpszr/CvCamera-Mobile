package com.os.cvCamera.features

import android.app.Activity
import android.os.Build
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.os.cvCamera.BuildConfig
import com.os.cvCamera.R
import org.opencv.android.OpenCVLoader

/**
 * Build and environment details behind the `about` menu item.
 */
class AboutDialog(
    private val activity: Activity,
) : CameraFeature {
    override fun onMenuItemSelected(itemId: Int): Boolean {
        if (itemId != R.id.about) return false

        val body =
            buildString {
                appendLine(activity.getString(R.string.about_app_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE))
                appendLine(activity.getString(R.string.about_git_hash, BuildConfig.GIT_HASH))
                appendLine()
                appendLine(activity.getString(R.string.about_opencv, OpenCVLoader.OPENCV_VERSION))
                appendLine()
                append(activity.getString(R.string.about_android, Build.VERSION.RELEASE, Build.VERSION.SDK_INT))
            }

        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.about)
            .setMessage(body)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        return true
    }
}
