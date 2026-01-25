package com.os.cvCamera
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.AttributeSet
import android.util.Size
import androidx.annotation.RequiresApi
import org.opencv.android.JavaCamera2View
import timber.log.Timber

class ExtendJavaCamera2View(context: Context, attrs: AttributeSet? = null) :
    JavaCamera2View(context, attrs) {

    /**
     * This method enables label with fps value on the screen with better size and position.
     */
    override fun enableFpsMeter() {
        if (mFpsMeter == null) {
            mFpsMeter = CvFpsMeter()
            mFpsMeter.setResolution(mFrameWidth, mFrameHeight)

        }
    }

    override fun disableFpsMeter() {
        mFpsMeter = null
    }

    fun getCameraDevice(): CameraDevice? {
        return mCameraDevice

    }

    fun setCameraResolution(width: Int, height: Int) {
        setMaxFrameSize(width, height)
        Timber.d("Camera resolution set to: $width x $height")
    }

    override fun calculateCameraFrameSize(supportedSizes: MutableList<*>, accessor: ListItemAccessor, surfaceWidth: Int, surfaceHeight: Int): org.opencv.core.Size {
        // OpenCV 4.11+ fixed the camera frame size calculation
        // https://github.com/opencv/opencv/issues/4704
        if (isOpenCVVersionAtLeast("4.11.0")) {
             Timber.d("OpenCV version is 4.11.0 or higher, using super.calculateCameraFrameSize")
             return super.calculateCameraFrameSize(supportedSizes, accessor, surfaceWidth, surfaceHeight)
        }
        
        Timber.d("calculateCameraFrameSize: supportedSizes=$supportedSizes, surfaceWidth=$surfaceWidth, surfaceHeight=$surfaceHeight")

        // Use the user-specified max resolution if available, otherwise use the surface size.
        // This allows setting a resolution higher than the view's size.
        val maxAllowedWidth = if (mMaxWidth != MAX_UNSPECIFIED) mMaxWidth else surfaceWidth
        val maxAllowedHeight = if (mMaxHeight != MAX_UNSPECIFIED) mMaxHeight else surfaceHeight

        var bestSize: org.opencv.core.Size? = null
        var bestArea = 0

        for (size in supportedSizes) {
            val currentWidth = accessor.getWidth(size)
            val currentHeight = accessor.getHeight(size)
            val currentArea = currentWidth * currentHeight

            if (currentWidth <= maxAllowedWidth && currentHeight <= maxAllowedHeight) {
                if (currentArea > bestArea) {
                    bestArea = currentArea
                    bestSize = org.opencv.core.Size(currentWidth.toDouble(), currentHeight.toDouble())
                }
            }
        }

        if (bestSize != null) {
            Timber.d("Selected camera frame size: ${bestSize.width}x${bestSize.height}")
            return bestSize
        }

        // Fallback to the first supported size if no suitable size is found
        if (supportedSizes.isNotEmpty()) {
            val firstSize = supportedSizes.first()
            val firstWidth = accessor.getWidth(firstSize)
            val firstHeight = accessor.getHeight(firstSize)
            Timber.d("Fallback to first supported size: ${firstWidth}x${firstHeight}")
            return org.opencv.core.Size(firstWidth.toDouble(), firstHeight.toDouble())
        }

        // Default fallback
        return org.opencv.core.Size(maxAllowedWidth.toDouble(), maxAllowedHeight.toDouble())
    }

    private fun isOpenCVVersionAtLeast(targetVersion: String): Boolean {
        try {
            val currentVersion = org.opencv.android.OpenCVLoader.OPENCV_VERSION
            val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
            val targetParts = targetVersion.split(".").map { it.toIntOrNull() ?: 0 }

            val length = maxOf(currentParts.size, targetParts.size)
            for (i in 0 until length) {
                val current = if (i < currentParts.size) currentParts[i] else 0
                val target = if (i < targetParts.size) targetParts[i] else 0
                if (current > target) return true
                if (current < target) return false
            }
            return true
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse OpenCV version")
            return false
        }
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
