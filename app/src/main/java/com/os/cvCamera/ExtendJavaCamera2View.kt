package com.os.cvCamera
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.view.WindowInsetsCompat
import org.opencv.android.JavaCamera2View
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min

/**
 * How the camera frame is mapped onto the view.
 */
enum class CanvasScaleMode {
    /** Whole frame visible, letterboxed with bars on the short axis (OpenCV's default). */
    FIT,

    /** Frame fills the view, overflow cropped. */
    FILL,
}

class ExtendJavaCamera2View(
    context: Context,
    attrs: AttributeSet? = null,
) : JavaCamera2View(context, attrs) {
    /**
     * Frame size explicitly asked for via [setCameraResolution], in sensor orientation.
     * [MAX_UNSPECIFIED] means "let OpenCV choose".
     */
    private var requestedWidth = MAX_UNSPECIFIED
    private var requestedHeight = MAX_UNSPECIFIED

    /** Surface size last handed to [connectCamera], needed to recompute the scale. */
    private var surfaceWidth = 0
    private var surfaceHeight = 0

    /**
     * Height of the status bar / cutout in px. The view draws edge to edge, so the FPS chip
     * needs this to avoid sitting under the system icons.
     */
    private var statusBarInset = 0f

    var canvasScaleMode: CanvasScaleMode = CanvasScaleMode.FIT
        set(value) {
            field = value
            // mScale is read while drawing every frame, so a new value is picked up by the
            // next frame. Changing the mode needs no camera restart.
            applyCanvasScale()
            Timber.d("Canvas scale mode: $value (mScale=$mScale)")
        }

    /** True while the FPS/resolution chip is being drawn. */
    val isFpsMeterEnabled: Boolean
        get() = mFpsMeter != null

    /**
     * Shows the FPS/resolution chip. Uses [CvFpsMeter] instead of OpenCV's plain white text.
     */
    override fun enableFpsMeter() {
        if (mFpsMeter == null) {
            mFpsMeter =
                CvFpsMeter(resources.displayMetrics.density).apply {
                    topInset = statusBarInset
                }
            mFpsMeter.setResolution(mFrameWidth, mFrameHeight)
        }
    }

    override fun disableFpsMeter() {
        mFpsMeter = null
    }

    /** Toggle the overlay. Applies on the next frame, so the camera keeps running. */
    fun toggleFpsMeter(): Boolean {
        if (isFpsMeterEnabled) disableFpsMeter() else enableFpsMeter()
        return isFpsMeterEnabled
    }

    fun getCameraDevice(): CameraDevice? = mCameraDevice

    /** True when the camera currently selected has a flash unit (front cameras usually do not). */
    fun hasFlash(): Boolean {
        val cameraId = mCameraID ?: return false
        return try {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            manager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } catch (e: CameraAccessException) {
            Timber.e(e, "Failed to query flash availability")
            false
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Unknown camera id while querying flash")
            false
        }
    }

    /**
     * Torch (continuous flash). Setting this re-issues the preview request, and the state is
     * re-applied automatically whenever the capture session is rebuilt.
     *
     * Ignored when the active camera has no flash.
     */
    var torchEnabled: Boolean = false
        set(value) {
            field = value && hasFlash()
            applyTorch()
        }

    /**
     * OpenCV configures the preview with [CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH], and under
     * that mode auto-exposure owns the flash and `FLASH_MODE` is ignored. Torch therefore requires
     * switching AE to plain [CaptureRequest.CONTROL_AE_MODE_ON].
     */
    private fun applyTorch() {
        val session = mCaptureSession ?: return
        val builder = mPreviewRequestBuilder ?: return

        try {
            if (torchEnabled) {
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
            } else {
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
            }
            session.setRepeatingRequest(builder.build(), null, mBackgroundHandler)
            Timber.d("Torch ${if (torchEnabled) "on" else "off"}")
        } catch (e: CameraAccessException) {
            Timber.e(e, "Failed to apply torch state")
        } catch (e: IllegalStateException) {
            // Session closed underneath us, e.g. mid camera switch. Retried on reconnect.
            Timber.w(e, "Capture session unavailable while applying torch")
        }
    }

    /** Re-apply the torch after every session rebuild (resolution change, camera switch, resume). */
    override fun allocateSessionStateCallback(): CameraCaptureSession.StateCallback {
        val base = super.allocateSessionStateCallback()
        return object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                base.onConfigured(session)
                // A camera without flash must not keep a stale "on" state.
                if (torchEnabled && !hasFlash()) torchEnabled = false else applyTorch()
            }

            override fun onConfigureFailed(session: CameraCaptureSession) = base.onConfigureFailed(session)

            override fun onClosed(session: CameraCaptureSession) = base.onClosed(session)
        }
    }

    /** Frame size currently in use, in view orientation. */
    fun getFrameSize(): android.util.Size = android.util.Size(mFrameWidth, mFrameHeight)

    /**
     * Request a specific camera frame size. Sizes come from [getSupportedPreviewSizes] and are
     * in sensor orientation. Takes effect on the next camera connect.
     */
    fun setCameraResolution(
        width: Int,
        height: Int,
    ) {
        requestedWidth = width
        requestedHeight = height
        setMaxFrameSize(width, height)
        Timber.d("Camera resolution requested: $width x $height")
    }

    /** Drop an explicit request and go back to OpenCV's automatic size selection. */
    fun clearCameraResolution() {
        requestedWidth = MAX_UNSPECIFIED
        requestedHeight = MAX_UNSPECIFIED
        setMaxFrameSize(MAX_UNSPECIFIED, MAX_UNSPECIFIED)
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        statusBarInset =
            WindowInsetsCompat
                .toWindowInsetsCompat(insets, this)
                .getInsets(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout())
                .top
                .toFloat()
        (mFpsMeter as? CvFpsMeter)?.topInset = statusBarInset
        return super.onApplyWindowInsets(insets)
    }

    override fun connectCamera(
        width: Int,
        height: Int,
    ): Boolean {
        surfaceWidth = width
        surfaceHeight = height
        val connected = super.connectCamera(width, height)
        // super computed mScale with Math.min (FIT); redo it if FILL was asked for.
        applyCanvasScale()
        return connected
    }

    /**
     * Recompute [mScale] for the active [canvasScaleMode].
     *
     * OpenCV centres the frame and scales it by `mScale`; a scale larger than the "fit" factor
     * simply overflows the canvas and is clipped, which is exactly a centre-crop.
     */
    private fun applyCanvasScale() {
        if (mFrameWidth <= 0 || mFrameHeight <= 0) return
        if (surfaceWidth <= 0 || surfaceHeight <= 0) return

        // OpenCV only scales when the view fills its parent; otherwise it draws 1:1.
        val lp = layoutParams
        if (lp != null &&
            (lp.width != ViewGroup.LayoutParams.MATCH_PARENT || lp.height != ViewGroup.LayoutParams.MATCH_PARENT)
        ) {
            mScale = 0f
            return
        }

        val scaleX = surfaceWidth.toFloat() / mFrameWidth
        val scaleY = surfaceHeight.toFloat() / mFrameHeight
        mScale =
            when (canvasScaleMode) {
                CanvasScaleMode.FILL -> max(scaleX, scaleY)
                CanvasScaleMode.FIT -> min(scaleX, scaleY)
            }
    }

    override fun calculateCameraFrameSize(
        supportedSizes: MutableList<*>,
        accessor: ListItemAccessor,
        surfaceWidth: Int,
        surfaceHeight: Int,
    ): org.opencv.core.Size {
        // An explicit request has to win even when it is larger than the preview surface.
        // CameraBridgeViewBase clamps mMaxWidth/mMaxHeight to the surface size:
        //     maxAllowed = (mMaxWidth != MAX_UNSPECIFIED && mMaxWidth < surfaceWidth) ? mMaxWidth : surfaceWidth
        // so anything bigger than the view is silently discarded and only downscaling works.
        if (requestedWidth != MAX_UNSPECIFIED && requestedHeight != MAX_UNSPECIFIED) {
            pickRequestedSize(supportedSizes, accessor)?.let { return it }
            Timber.w("Requested ${requestedWidth}x$requestedHeight is not supported, falling back")
        }

        // OpenCV 4.11+ fixed the automatic camera frame size calculation
        // https://github.com/opencv/opencv/issues/4704
        return super.calculateCameraFrameSize(supportedSizes, accessor, surfaceWidth, surfaceHeight)
    }

    /** Exact match for the requested size, else the largest supported size that fits inside it. */
    private fun pickRequestedSize(
        supportedSizes: MutableList<*>,
        accessor: ListItemAccessor,
    ): org.opencv.core.Size? {
        var best: org.opencv.core.Size? = null
        var bestArea = 0

        for (size in supportedSizes) {
            val width = accessor.getWidth(size)
            val height = accessor.getHeight(size)

            if (width == requestedWidth && height == requestedHeight) {
                Timber.d("Using requested camera frame size: ${width}x$height")
                return org.opencv.core.Size(width.toDouble(), height.toDouble())
            }

            if (width <= requestedWidth && height <= requestedHeight && width * height > bestArea) {
                bestArea = width * height
                best = org.opencv.core.Size(width.toDouble(), height.toDouble())
            }
        }

        if (best != null) {
            Timber.d("Closest supported frame size: ${best.width}x${best.height}")
        }
        return best
    }

    fun getSupportedPreviewSizes(): List<android.util.Size> {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val sizes = mutableListOf<android.util.Size>()
        try {
            val cameraId = mCameraID ?: return emptyList()
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            // Use SurfaceTexture for preview sizes
            val outputSizes = map?.getOutputSizes(android.graphics.SurfaceTexture::class.java)
            if (outputSizes != null) {
                sizes.addAll(outputSizes)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get supported preview sizes")
        }
        return sizes
    }
}
