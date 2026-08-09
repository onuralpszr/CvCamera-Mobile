package com.os.cvCamera

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.get
import androidx.core.view.size
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.os.cvCamera.BuildConfig.GIT_HASH
import com.os.cvCamera.BuildConfig.VERSION_NAME
import com.os.cvCamera.databinding.ActivityMainBinding
import com.os.cvCamera.features.CameraFeature
import com.os.cvCamera.features.CanvasScaleControl
import com.os.cvCamera.features.ControlsRotator
import com.os.cvCamera.features.FaceDetection
import com.os.cvCamera.features.FilterPicker
import com.os.cvCamera.features.FpsOverlayControl
import com.os.cvCamera.features.PhotoOrientation
import com.os.cvCamera.features.ResolutionPicker
import com.os.cvCamera.features.ShutterEffect
import com.os.cvCamera.features.TorchControl
import com.os.cvCamera.features.leafViews
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CAMERA_ID_BACK
import org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.OpenCVLoader.OPENCV_VERSION
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity :
    CameraActivity(),
    CvCameraViewListener2 {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mRGBA: Mat
    private lateinit var mRGBAT: Mat
    private var mCameraId: Int = CAMERA_ID_BACK
    private lateinit var mCameraManager: CameraManager

    // Photo capture
    private var mCaptureNextFrame = false

    /**
     * Every optional capability, in one place. Order matters: [CameraFeature.processFrame] runs in
     * this order, so effects apply before face boxes are drawn on top.
     *
     * Add a feature by adding a line; remove one by deleting its line and its file. Nothing else
     * in the app refers to them.
     */
    private val features: List<CameraFeature> by lazy {
        listOf(
            FilterPicker(this),
            FaceDetection(this),
            ResolutionPicker(this, binding.CvCamera),
            CanvasScaleControl(this, binding.CvCamera),
            FpsOverlayControl(this, binding.CvCamera),
            TorchControl(this, binding.bottomAppBar.menu, binding.CvCamera),
            ShutterEffect(binding.root),
            PhotoOrientation(this),
            ControlsRotator(this) { rotatableControls() },
        )
    }

    companion object {
        init {
            System.loadLibrary("opencv_java4")
            System.loadLibrary("cvcamera")
        }
    }

    private external fun openCVVersion(): String?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("OpenCV Version: $OPENCV_VERSION")

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mCameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        // Get the camera ID string for the back camera
        // Get the camera ID string for the back camera
        initCameraConfigs()

        // Load buttonConfigs
        configButtons()

        // Load button colors
        setButtonColors()

        features.forEach { it.attach() }
    }

    private fun setButtonColors() {
        for (i in 0..<binding.bottomAppBar.menu.size) {
            val item = binding.bottomAppBar.menu[i]
            val typedValue = TypedValue()
            theme.resolveAttribute(android.R.attr.colorAccent, typedValue, true)
            item.icon?.colorFilter =
                PorterDuffColorFilter(typedValue.data, PorterDuff.Mode.SRC_ATOP)
        }
    }

    private fun configButtons() {
        binding.cvCameraChangeFab.setOnClickListener {
            cameraSwitch()
        }

        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.about -> {
                    val cvVer = openCVVersion() // Get OpenCV version from native code
                    Toast
                        .makeText(
                            this,
                            "CvCamera-Mobile - Version $VERSION_NAME-$GIT_HASH - OpenCV $cvVer ",
                            Toast.LENGTH_SHORT,
                        ).show()
                    true
                }

                R.id.capturePhoto -> {
                    // The shutter flash and sound are the feedback here; the saved-photo toast
                    // follows once the file is written.
                    mCaptureNextFrame = true
                    true
                }

                // Everything else belongs to a feature.
                else -> {
                    features.any { it.onMenuItemSelected(menuItem.itemId) }
                }
            }
        }
    }

    private fun cameraSwitch() {
        mCameraId =
            if (mCameraId == CAMERA_ID_BACK) {
                CAMERA_ID_FRONT
            } else {
                CAMERA_ID_BACK
            }

        features.forEach { it.onCameraSwitched() }

        binding.CvCamera.disableView()
        binding.CvCamera.setCameraIndex(mCameraId)
        binding.CvCamera.enableView()
    }

    private fun initCameraConfigs() {
        binding.CvCamera.setCameraIndex(mCameraId)
        binding.CvCamera.setCvCameraViewListener(this)
        Timber.d("OpenCV Camera Configured")
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase> = listOf(binding.CvCamera)

    override fun onCameraPermissionGranted() {
        super.onCameraPermissionGranted()
        binding.CvCamera.enableView()
    }

    // WIP Flashlight Support
    override fun onCameraViewStarted(
        width: Int,
        height: Int,
    ) {
        mRGBA = Mat(height, width, CvType.CV_8UC4)
        mRGBAT = Mat()
    }

    override fun onCameraViewStopped() {
        mRGBA.release()
        mRGBAT.release()
    }

    override fun onCameraFrame(inputFrame: CvCameraViewFrame?): Mat {
        if (inputFrame == null) return mRGBA // return last or empty frame

        val source =
            if (mCameraId == CAMERA_ID_BACK) {
                mRGBA = inputFrame.rgba()
                mRGBA
            } else {
                mRGBA = inputFrame.rgba()
                // Flipping to show portrait mode properly
                Core.flip(mRGBA, mRGBAT, 1)
                // Release the matrix to avoid memory leaks
                mRGBA.release()
                mRGBAT
            }

        // Features transform the frame in registration order.
        val finalFrame = features.fold(source) { frame, feature -> feature.processFrame(frame) }

        if (mCaptureNextFrame) {
            mCaptureNextFrame = false
            captureFrame(finalFrame)
        }

        return finalFrame
    }

    private fun captureFrame(frame: Mat) {
        // Fire on the frame that is actually saved, so shutter feedback lines up with capture.
        features.forEach { it.onPhotoCaptureStarted() }
        try {
            val bitmap = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(frame, bitmap)

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "CvCamera_$timestamp.jpg"

            val contentValues =
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CvCamera")
                }

            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                }
                features.forEach { feature -> feature.onPhotoSaved(it) }
                runOnUiThread {
                    Toast.makeText(this, getString(R.string.photo_saved, filename), Toast.LENGTH_SHORT).show()
                }
                Timber.d("Photo saved: $filename")
            }
        } catch (e: Exception) {
            Timber.e("Error saving photo: ${e.message}")
            runOnUiThread {
                Toast.makeText(this, getString(R.string.photo_save_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
        binding.CvCamera.disableView()
        features.forEach { it.detach() }
    }

    override fun onPause() {
        Timber.d("onPause")
        super.onPause()
        binding.CvCamera.disableView()
        features.forEach { it.onPause() }
    }

    override fun onResume() {
        Timber.d("onResume")
        super.onResume()
        features.forEach { it.onResume() }
    }

    /**
     * Icons that should spin with the device: the bottom bar's action items (including the
     * overflow button) and the camera-switch FAB. Resolved lazily on each rotation because menu
     * item views are inflated after [onCreate].
     */
    private fun rotatableControls(): List<View> = binding.bottomAppBar.leafViews() + binding.cvCameraChangeFab
}
