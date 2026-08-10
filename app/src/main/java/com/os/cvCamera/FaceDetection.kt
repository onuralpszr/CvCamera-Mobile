package com.os.cvCamera

import android.app.Activity
import android.widget.Toast
import com.os.cvcamera.ui.R
import com.os.cvcamera.ui.features.CameraFeature
import org.opencv.core.Mat

/**
 * Haar cascade face detection, with detected faces drawn onto the frame.
 *
 * Toggled by the `faceDetection` menu item; detection itself lives in `FaceDetector.kt` and uses
 * the cascade in `res/raw`.
 *
 * OpenCV 5 moved `CascadeClassifier` to `opencv_contrib/xobjdetect`, which the prebuilt Android
 * SDK does not ship, so this implementation is 4.x only. A 5.x port needs the DNN-based
 * `FaceDetectorYN`.
 */
class FaceDetection(
    private val activity: Activity,
) : CameraFeature {
    private var detector: FaceDetector? = null
    private var enabled = false

    override fun attach() {
        detector = FaceDetector(activity)
    }

    override fun detach() {
        detector = null
    }

    override fun onMenuItemSelected(itemId: Int): Boolean {
        if (itemId != R.id.faceDetection) return false

        enabled = !enabled
        val label = if (enabled) R.string.face_detection_on else R.string.face_detection_off
        Toast.makeText(activity, activity.getString(label), Toast.LENGTH_SHORT).show()
        return true
    }

    override fun processFrame(frame: Mat): Mat =
        if (enabled) {
            detector?.detect(frame) ?: frame
        } else {
            frame
        }
}
