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

    // Filters id
    private var mFilterId = -1

    // Face detection
    private var mFaceDetector: FaceDetector? = null
    private var mFaceDetectionEnabled = false

    // Photo capture
    private var mCaptureNextFrame = false

    // Shutter flash + sound. Self-contained: drop this line, the play() and release() calls,
    // and ShutterEffect.kt to remove the feature entirely.
    private val shutterEffect by lazy { ShutterEffect(binding.root) }

    // Rotates only the control icons while the bar stays at the physical bottom.
    private val controlsRotator by lazy { ControlsRotator(this) { rotatableControls() } }

    companion object {
        init {
            System.loadLibrary("opencv_java4")
            System.loadLibrary("cvcamera")
        }

        // Filter constants
        const val FILTER_NONE = -1
        const val FILTER_GRAY = 0
        const val FILTER_SEPIA = 1
        const val FILTER_BLUR = 2
        const val FILTER_HSV = 3
        const val FILTER_EDGE = 4
        const val FILTER_SOBEL = 5
        const val FILTER_CANNY = 6
        const val FILTER_NEGATIVE = 7
        const val FILTER_SHARPEN = 8
        const val FILTER_EMBOSS = 9
        const val FILTER_CARTOON = 10
        const val FILTER_BINARY = 11
        const val FILTER_SKETCH = 12
        const val FILTER_CONTOURS = 13
        const val FILTER_POSTERIZE = 14
        const val FILTER_VIGNETTE = 15
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

        // Initialize face detection
        mFaceDetector = FaceDetector(this)

        // Load buttonConfigs
        configButtons()

        // Load button colors
        setButtonColors()
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
                    // Get app version and githash from BuildConfig
                    val cvVer = openCVVersion() // Get OpenCV version from native code
                    val toast: Toast =
                        Toast.makeText(
                            this,
                            "CvCamera-Mobile - Version $VERSION_NAME-$GIT_HASH - OpenCV $cvVer ",
                            Toast.LENGTH_SHORT,
                        )
                    toast.show()

                    true
                }

                R.id.filters -> {
                    showFilterDialog()
                    true
                }

                R.id.faceDetection -> {
                    mFaceDetectionEnabled = !mFaceDetectionEnabled
                    val message =
                        if (mFaceDetectionEnabled) {
                            getString(R.string.face_detection_on)
                        } else {
                            getString(R.string.face_detection_off)
                        }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.capturePhoto -> {
                    // The shutter flash and sound are the feedback here; the saved-photo toast
                    // follows once the file is written.
                    mCaptureNextFrame = true
                    true
                }

                R.id.resizeCanvas -> {
                    // mScale is re-read for every drawn frame, so this needs no camera restart.
                    val mode =
                        if (binding.CvCamera.canvasScaleMode == CanvasScaleMode.FIT) {
                            CanvasScaleMode.FILL
                        } else {
                            CanvasScaleMode.FIT
                        }
                    binding.CvCamera.canvasScaleMode = mode
                    val label =
                        if (mode == CanvasScaleMode.FILL) {
                            getString(R.string.canvas_fill)
                        } else {
                            getString(R.string.canvas_fit)
                        }
                    Toast.makeText(this, label, Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.toggleFps -> {
                    val shown = binding.CvCamera.toggleFpsMeter()
                    val label = if (shown) R.string.fps_overlay_on else R.string.fps_overlay_off
                    Toast.makeText(this, getString(label), Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.cameraResolution -> {
                    showResolutionDialog()
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    /**
     * Selectable effects, in the order shown. "No effect" is first so it is the obvious default.
     */
    private val filterEntries =
        listOf(
            FILTER_NONE to R.string.no_filter,
            FILTER_GRAY to R.string.grayscale_filter,
            FILTER_SEPIA to R.string.sepia_filter,
            FILTER_BLUR to R.string.blur_filter,
            FILTER_HSV to R.string.hsv_filter,
            FILTER_EDGE to R.string.edge_filter,
            FILTER_SOBEL to R.string.sobel_filter,
            FILTER_CANNY to R.string.canny_filter,
            FILTER_NEGATIVE to R.string.negative_filter,
            FILTER_SHARPEN to R.string.sharpen_filter,
            FILTER_EMBOSS to R.string.emboss_filter,
            FILTER_CARTOON to R.string.cartoon_filter,
            FILTER_BINARY to R.string.binary_filter,
            FILTER_SKETCH to R.string.sketch_filter,
            FILTER_CONTOURS to R.string.contours_filter,
            FILTER_POSTERIZE to R.string.posterize_filter,
            FILTER_VIGNETTE to R.string.vignette_filter,
        )

    /**
     * Single-choice effect picker. Replaces cycling through every filter one tap at a time.
     * Filters are applied per frame, so switching is instant and never restarts the camera.
     */
    private fun showFilterDialog() {
        val labels = filterEntries.map { getString(it.second) }.toTypedArray()
        val checked = filterEntries.indexOfFirst { it.first == mFilterId }.coerceAtLeast(0)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.filters)
            .setSingleChoiceItems(labels, checked) { dialog, which ->
                dialog.dismiss()
                mFilterId = filterEntries[which].first
                Timber.d("Filter selected: ${getString(filterEntries[which].second)}")
            }.setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * Single-choice resolution picker, largest first, with the size currently in use pre-selected.
     * The camera is only restarted when the choice actually changes.
     */
    private fun showResolutionDialog() {
        val sizes =
            binding.CvCamera
                .getSupportedPreviewSizes()
                .distinct()
                .sortedByDescending { it.width.toLong() * it.height }

        if (sizes.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_resolutions), Toast.LENGTH_SHORT).show()
            return
        }

        val labels = sizes.map { describeSize(it) }.toTypedArray()
        // The frame size is reported in view orientation, the sizes in sensor orientation.
        val frame = binding.CvCamera.getFrameSize()
        val checked =
            sizes.indexOfFirst {
                (it.width == frame.width && it.height == frame.height) ||
                    (it.width == frame.height && it.height == frame.width)
            }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.cameraResolution)
            .setSingleChoiceItems(labels, checked) { dialog, which ->
                dialog.dismiss()
                val selected = sizes[which]
                if (which == checked) return@setSingleChoiceItems
                Toast
                    .makeText(
                        this,
                        getString(R.string.switching_resolution, selected.width, selected.height),
                        Toast.LENGTH_SHORT,
                    ).show()
                // Let the dialog finish dismissing before the camera restart so the
                // reconnect does not stutter the dismiss animation.
                binding.CvCamera.post {
                    applyResolution(selected.width, selected.height)
                }
            }.setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun applyResolution(
        width: Int,
        height: Int,
    ) {
        binding.CvCamera.disableView()
        binding.CvCamera.setCameraResolution(width, height)
        binding.CvCamera.enableView()
    }

    /** e.g. `1920 × 1080  ·  16:9  ·  2.1 MP` */
    private fun describeSize(size: android.util.Size): String {
        val megaPixels = size.width.toLong() * size.height / 1_000_000.0
        return "${size.width} × ${size.height}  ·  ${aspectRatioOf(size)}  ·  ${"%.1f".format(megaPixels)} MP"
    }

    private fun aspectRatioOf(size: android.util.Size): String {
        val divisor = gcd(size.width, size.height)
        return "${size.width / divisor}:${size.height / divisor}"
    }

    private tailrec fun gcd(
        a: Int,
        b: Int,
    ): Int = if (b == 0) (if (a == 0) 1 else a) else gcd(b, a % b)

    private fun cameraSwitch() {
        mCameraId =
            if (mCameraId == CAMERA_ID_BACK) {
                CAMERA_ID_FRONT
            } else {
                CAMERA_ID_BACK
            }

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
//    private fun findFlashLight() {
//        for (cameraId in mCameraManager.cameraIdList) {
//            try {
//                // Check if the camera has a torchlight
//                val hasTorch = mCameraManager.getCameraCharacteristics(cameraId)
//                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
//
//                if (hasTorch) {
//                    // Find the ID of the camera that has a torchlight and store it in mTorchCameraId
//                    Timber.d("Torch is available")
//                    Timber.d("Camera Id: $cameraId")
//                    mTorchCameraId = cameraId
//                    mTorchState = false
//                    break
//                } else {
//                    Timber.d("Torch is not available")
//                }
//            } catch (e: CameraAccessException) {
//                // Handle any errors that occur while trying to access the camera
//                Timber.e("CameraAccessException ${e.message}")
//            }
//        }
//    }

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

    override fun onCameraFrame(inputFrame: CvCameraViewFrame?): Mat =
        if (inputFrame != null) {
            val processedFrame =
                if (mCameraId == CAMERA_ID_BACK) {
                    mRGBA = inputFrame.rgba()
                    cvFilters(mRGBA)
                } else {
                    mRGBA = inputFrame.rgba()
                    // Flipping to show portrait mode properly
                    Core.flip(mRGBA, mRGBAT, 1)
                    // Release the matrix to avoid memory leaks
                    mRGBA.release()
                    // Check if grayscale is enabled
                    cvFilters(mRGBAT)
                }

            // Apply face detection if enabled
            val finalFrame =
                if (mFaceDetectionEnabled) {
                    mFaceDetector?.detect(processedFrame) ?: processedFrame
                } else {
                    processedFrame
                }

            // Capture photo if requested
            if (mCaptureNextFrame) {
                mCaptureNextFrame = false
                captureFrame(finalFrame)
            }

            finalFrame
        } else {
            // return last or empty frame
            mRGBA
        }

    private fun captureFrame(frame: Mat) {
        // Fire on the frame that is actually saved, so the flash lines up with the capture.
        // play() marshals itself onto the UI thread, so calling from the camera thread is fine.
        shutterEffect.play()
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

    private fun cvFilters(frame: Mat): Mat =
        when (mFilterId) {
            FILTER_GRAY -> frame.toGray()
            FILTER_SEPIA -> frame.toSepia()
            FILTER_BLUR -> frame.toBlur()
            FILTER_HSV -> frame.toHSV()
            FILTER_EDGE -> frame.toEdgeDetection()
            FILTER_SOBEL -> frame.toSobel()
            FILTER_CANNY -> frame.toCanny()
            FILTER_NEGATIVE -> frame.toNegative()
            FILTER_SHARPEN -> frame.toSharpen()
            FILTER_EMBOSS -> frame.toEmboss()
            FILTER_CARTOON -> frame.toCartoon()
            FILTER_BINARY -> frame.toBinary()
            FILTER_SKETCH -> frame.toSketch()
            FILTER_CONTOURS -> frame.toContours()
            FILTER_POSTERIZE -> frame.toPosterize()
            FILTER_VIGNETTE -> frame.toVignette()
            else -> frame
        }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
        binding.CvCamera.disableView()
        shutterEffect.release()
    }

    override fun onPause() {
        Timber.d("onPause")
        super.onPause()
        binding.CvCamera.disableView()
        controlsRotator.stop()
    }

    override fun onResume() {
        Timber.d("onResume")
        super.onResume()
        controlsRotator.start()
    }

    /**
     * Icons that should spin with the device: the bottom bar's action items (including the
     * overflow button) and the camera-switch FAB. Resolved lazily on each rotation because menu
     * item views are inflated after [onCreate].
     */
    private fun rotatableControls(): List<View> = binding.bottomAppBar.leafViews() + binding.cvCameraChangeFab
}
