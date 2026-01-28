package com.os.cvCamera

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import org.opencv.android.FpsMeter
import org.opencv.core.Core
import java.text.DecimalFormat

class CvFpsMeter : FpsMeter() {
    private val step = 20
    private val fpsFormat = DecimalFormat("0.00")

    private var mFramesCounter = 0
    private var mFrequency = 0.0
    private var mprevFrameTime: Long = 0
    private var mStrfps: String? = null
    private var mPaint: Paint? = null
    private var mIsInitialized = false
    private var mWidth = 0
    private var mHeight = 0
    private var mExtraOffsetX = 50f
    private var mExtraOffsetY = 120f

    override fun init() {
        mFramesCounter = 0
        mFrequency = Core.getTickFrequency()
        mprevFrameTime = Core.getTickCount()
        mStrfps = ""
        mPaint = Paint()
        mPaint!!.color = Color.WHITE
        mPaint!!.textSize = 100f
    }

    override fun measure() {
        if (!mIsInitialized) {
            init()
            mIsInitialized = true
        } else {
            mFramesCounter++
            if (mFramesCounter % step == 0) {
                val time = Core.getTickCount()
                val fps = step * mFrequency / (time - mprevFrameTime)
                mprevFrameTime = time
                mStrfps =
                    if (mWidth != 0 && mHeight != 0) {
                        fpsFormat.format(fps) + " FPS@" +
                            Integer.valueOf(
                                mWidth,
                            ) + "x" + Integer.valueOf(mHeight)
                    } else {
                        fpsFormat.format(fps) + " FPS"
                    }
            }
        }
    }

    override fun setResolution(
        width: Int,
        height: Int,
    ) {
        mWidth = width
        mHeight = height
    }

    override fun draw(
        canvas: Canvas,
        offsetx: Float,
        offsety: Float,
    ) {
        canvas.drawText(mStrfps!!, offsetx + mExtraOffsetX, offsety + mExtraOffsetY, mPaint!!)
    }
}
