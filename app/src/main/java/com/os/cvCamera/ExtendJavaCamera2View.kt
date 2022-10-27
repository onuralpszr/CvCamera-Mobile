package com.os.cvCamera
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCamera2View
import org.opencv.android.Utils
import org.opencv.core.Mat

class ExtendJavaCamera2View(context: Context, attrs: AttributeSet? = null) :
    JavaCamera2View(context, attrs) {


    private val mMatrix: Matrix = Matrix()
    private var mListener: CvCameraViewListener2? = null
    private var mCacheBitmap: Bitmap? = null
    private val TAG = this.javaClass.name
    private val mListenerField = CameraBridgeViewBase::class.java.getDeclaredField("mListener")

    private fun updateMatrix() {
        val mw = this.width.toFloat()
        val mh = this.height.toFloat()
        val hw = this.width / 2.0f
        val hh = this.height / 2.0f
        val cw = Resources.getSystem().displayMetrics.widthPixels.toFloat()
        val ch = Resources.getSystem().displayMetrics.heightPixels.toFloat()
        var scale = cw / mh
        val scale2 = ch / mw
        if (scale2 > scale) {
            scale = scale2
        }
        mMatrix.reset()
        if (mCameraIndex == CAMERA_ID_FRONT) {
            mMatrix.preScale(-1f, 1f, hw, hh) //MH - this will mirror the camera
        }

        mMatrix.preTranslate(hw, hh)
        if (mCameraIndex == CAMERA_ID_FRONT) {
            mMatrix.preRotate(270f)
        } else {
            mMatrix.preRotate(90f)
        }
        mMatrix.preTranslate(-hw, -hh)
        mMatrix.preScale(scale, scale, hw, hh)
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        super.layout(l, t, r, b)
        updateMatrix()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        updateMatrix()
    }

    override fun deliverAndDrawFrame(frame: CvCameraViewFrame?) {
        mListenerField.isAccessible = true
        mListener = mListenerField.get(this) as CvCameraViewListener2


        val modified: Mat? = if (mListener != null) {
            mListener!!.onCameraFrame(frame)
        } else {
            frame!!.rgba()
        }

        var bmpValid = true
        if (modified != null) {
            try {
                Utils.matToBitmap(modified, mCacheBitmap)
            } catch (e: Exception) {
                Log.e(TAG, "Mat type: $modified")
                Log.e(TAG, "Bitmap type: " + mCacheBitmap!!.width + "*" + mCacheBitmap!!.height)
                Log.e(TAG, "Utils.matToBitmap() throws an exception: " + e.message)
                bmpValid = false
            }
        }

        if (bmpValid && mCacheBitmap != null) {
            val canvas: Canvas? = holder.lockCanvas()
            if (canvas != null) {
                canvas.drawColor(0, PorterDuff.Mode.CLEAR)
                val saveCount: Int = canvas.save()
                canvas.setMatrix(mMatrix)
                if (mScale != 0f) {
                    canvas.drawBitmap(
                        mCacheBitmap!!, Rect(0, 0, mCacheBitmap!!.width, mCacheBitmap!!.height),
                        Rect(
                            ((canvas.width - mScale * mCacheBitmap!!.width) / 2).toInt(),
                            ((canvas.height - mScale * mCacheBitmap!!.height) / 2).toInt(),
                            ((canvas.width - mScale * mCacheBitmap!!.width) / 2 + mScale * mCacheBitmap!!.width).toInt(),
                            ((canvas.height - mScale * mCacheBitmap!!.height) / 2 + mScale * mCacheBitmap!!.height).toInt()
                        ), null
                    )
                } else {
                    canvas.drawBitmap(
                        mCacheBitmap!!, Rect(0, 0, mCacheBitmap!!.width, mCacheBitmap!!.height),
                        Rect(
                            (canvas.width - mCacheBitmap!!.width) / 2,
                            (canvas.height - mCacheBitmap!!.height) / 2,
                            (canvas.width - mCacheBitmap!!.width) / 2 + mCacheBitmap!!.width,
                            (canvas.height - mCacheBitmap!!.height) / 2 + mCacheBitmap!!.height
                        ), null
                    )
                }

                //Restore canvas after draw bitmap
                canvas.restoreToCount(saveCount)
                if (mFpsMeter != null) {
                    mFpsMeter.measure()
                    mFpsMeter.draw(canvas, 20f, 30f)
                }
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    override fun AllocateCache() {
        mCacheBitmap = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888)
    }

}




