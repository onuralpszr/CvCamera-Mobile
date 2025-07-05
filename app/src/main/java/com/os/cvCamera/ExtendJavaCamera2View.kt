package com.os.cvCamera
import android.content.Context
import android.hardware.camera2.CameraDevice
import android.util.AttributeSet
import org.opencv.android.JavaCamera2View

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

}
