package com.os.cvcamera.ui

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.get
import androidx.core.view.size
import com.os.cvcamera.ui.databinding.ViewCameraScreenBinding
import com.os.cvcamera.ui.features.CameraFeature
import com.os.cvcamera.ui.features.CanvasScaleControl
import com.os.cvcamera.ui.features.ControlsRotator
import com.os.cvcamera.ui.features.FpsOverlayControl
import com.os.cvcamera.ui.features.PhotoOrientation
import com.os.cvcamera.ui.features.ResolutionPicker
import com.os.cvcamera.ui.features.ShutterEffect
import com.os.cvcamera.ui.features.TorchControl
import com.os.cvcamera.ui.features.leafViews
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CAMERA_ID_BACK
import org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The complete camera screen: preview, bottom bar, capture to `MediaStore`, front and back
 * switching, and the [CameraFeature] pipeline.
 *
 * Every example app extends this and overrides [createFeatures] to add whatever is specific to its
 * language, so the shared UI and behaviour is defined once here.
 *
 * Subclasses must load the OpenCV native library before `super.onCreate` runs, since inflating the
 * preview touches OpenCV classes.
 */
abstract class CameraScreenActivity :
    CameraActivity(),
    CvCameraViewListener2 {
    protected lateinit var binding: ViewCameraScreenBinding
        private set

    /** Camera currently selected, one of the `CameraBridgeViewBase.CAMERA_ID_*` constants. */
    private var cameraId: Int = CAMERA_ID_BACK

    private lateinit var rgba: Mat
    private lateinit var rgbaFlipped: Mat
    private var captureNextFrame = false

    /**
     * Features contributed by the app, evaluated once after the views exist.
     *
     * Order matters: [CameraFeature.processFrame] runs in list order, so effects should come before
     * anything that draws on top of them. The result is appended to [defaultFeatures].
     */
    protected abstract fun createFeatures(): List<CameraFeature>

    /**
     * Features every example shares: torch, canvas scaling, the FPS overlay, resolution selection,
     * shutter feedback, EXIF orientation and rotating control icons.
     *
     * Call this from [createFeatures], or drop entries from the returned list to leave one out.
     */
    protected fun defaultFeatures(): List<CameraFeature> =
        listOf(
            ResolutionPicker(this, binding.cameraView),
            CanvasScaleControl(this, binding.cameraView),
            FpsOverlayControl(this, binding.cameraView),
            TorchControl(this, binding.bottomAppBar.menu, binding.cameraView),
            ShutterEffect(binding.root),
            PhotoOrientation(this),
            ControlsRotator(this) { rotatableControls() },
        )

    private val features: List<CameraFeature> by lazy { createFeatures() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ViewCameraScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cameraView.setCameraIndex(cameraId)
        binding.cameraView.setCvCameraViewListener(this)

        binding.cameraSwitchFab.setOnClickListener { switchCamera() }
        binding.bottomAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                // Shutter feedback and the saved photo toast are the confirmation here.
                R.id.capturePhoto -> {
                    captureNextFrame = true
                    true
                }

                else -> {
                    features.any { it.onMenuItemSelected(item.itemId) }
                }
            }
        }

        tintMenuIcons()
        features.forEach { it.attach() }
    }

    private fun tintMenuIcons() {
        val accent = TypedValue()
        theme.resolveAttribute(android.R.attr.colorAccent, accent, true)
        for (index in 0..<binding.bottomAppBar.menu.size) {
            binding.bottomAppBar.menu[index]
                .icon
                ?.colorFilter =
                PorterDuffColorFilter(accent.data, PorterDuff.Mode.SRC_ATOP)
        }
    }

    private fun switchCamera() {
        cameraId = if (cameraId == CAMERA_ID_BACK) CAMERA_ID_FRONT else CAMERA_ID_BACK
        features.forEach { it.onCameraSwitched() }

        binding.cameraView.disableView()
        binding.cameraView.setCameraIndex(cameraId)
        binding.cameraView.enableView()
    }

    /**
     * Icons that rotate with the device: the bottom bar action items including the overflow button,
     * plus the camera switch button. Resolved on each rotation because menu item views are
     * inflated after [onCreate].
     */
    private fun rotatableControls(): List<View> = binding.bottomAppBar.leafViews() + binding.cameraSwitchFab

    override fun getCameraViewList(): List<CameraBridgeViewBase> = listOf(binding.cameraView)

    override fun onCameraPermissionGranted() {
        super.onCameraPermissionGranted()
        binding.cameraView.enableView()
    }

    override fun onCameraViewStarted(
        width: Int,
        height: Int,
    ) {
        rgba = Mat(height, width, CvType.CV_8UC4)
        rgbaFlipped = Mat()
    }

    override fun onCameraViewStopped() {
        rgba.release()
        rgbaFlipped.release()
    }

    override fun onCameraFrame(inputFrame: CvCameraViewFrame?): Mat {
        if (inputFrame == null) return rgba

        val source =
            if (cameraId == CAMERA_ID_BACK) {
                rgba = inputFrame.rgba()
                rgba
            } else {
                rgba = inputFrame.rgba()
                // The front camera is mirrored, so flip it to match what the user sees.
                Core.flip(rgba, rgbaFlipped, 1)
                rgba.release()
                rgbaFlipped
            }

        val processed = features.fold(source) { frame, feature -> feature.processFrame(frame) }

        if (captureNextFrame) {
            captureNextFrame = false
            savePhoto(processed)
        }
        return processed
    }

    private fun savePhoto(frame: Mat) {
        // Fire on the frame that is actually saved, so shutter feedback lines up with capture.
        features.forEach { it.onPhotoCaptureStarted() }
        try {
            val bitmap = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(frame, bitmap)

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "CvCamera_$timestamp.jpg"

            val values =
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CvCamera")
                }

            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                contentResolver.openOutputStream(it)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
                }
                features.forEach { feature -> feature.onPhotoSaved(it) }
                runOnUiThread {
                    Toast.makeText(this, getString(R.string.photo_saved, filename), Toast.LENGTH_SHORT).show()
                }
                Timber.d("Photo saved: %s", filename)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving photo")
            runOnUiThread {
                Toast.makeText(this, getString(R.string.photo_save_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        features.forEach { it.onResume() }
    }

    override fun onPause() {
        super.onPause()
        binding.cameraView.disableView()
        features.forEach { it.onPause() }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.cameraView.disableView()
        features.forEach { it.detach() }
    }

    private companion object {
        const val JPEG_QUALITY = 95
    }
}
