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
