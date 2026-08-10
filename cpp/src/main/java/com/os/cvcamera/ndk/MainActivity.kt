package com.os.cvcamera.ndk

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.core.Mat
import timber.log.Timber

/**
 * OpenCV NDK example.
 *
 * The camera preview is set up in Kotlin, but every frame is processed in C++ through
 * [NativeProcessor]. Tapping the screen cycles the native operation.
 *
 * This is the counterpart to the Kotlin example in `app/`, which does all of its processing
 * through the OpenCV Java bindings instead.
 */
class MainActivity :
    CameraActivity(),
    CvCameraViewListener2 {
    private lateinit var cameraView: CameraBridgeViewBase

    /** Native operation applied to each frame. */
    private enum class Mode { NONE, GREYSCALE, CANNY }

    private var mode = Mode.CANNY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        Timber.plant(Timber.DebugTree())
        Timber.d("OpenCV native version: %s", NativeProcessor.openCvVersion())

        cameraView = findViewById(R.id.cameraView)
        cameraView.setCvCameraViewListener(this)
        cameraView.setOnClickListener { cycleMode() }
    }

    private fun cycleMode() {
        mode =
            when (mode) {
                Mode.CANNY -> Mode.GREYSCALE
                Mode.GREYSCALE -> Mode.NONE
                Mode.NONE -> Mode.CANNY
            }
        Toast.makeText(this, getString(R.string.mode_changed, mode.name), Toast.LENGTH_SHORT).show()
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase> = listOf(cameraView)

    override fun onCameraPermissionGranted() {
        super.onCameraPermissionGranted()
        cameraView.enableView()
    }

    override fun onCameraViewStarted(
        width: Int,
        height: Int,
    ) = Unit

    override fun onCameraViewStopped() = Unit

    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
        val rgba = inputFrame.rgba()
        when (mode) {
            Mode.CANNY -> NativeProcessor.cannyEdges(rgba)
            Mode.GREYSCALE -> NativeProcessor.greyscale(rgba)
            Mode.NONE -> Unit
        }
        return rgba
    }

    override fun onPause() {
        super.onPause()
        cameraView.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraView.disableView()
    }
}
