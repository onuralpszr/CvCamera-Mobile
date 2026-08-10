package com.os.cvcamera.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import org.opencv.android.FpsMeter
import org.opencv.core.Core
import java.text.DecimalFormat

/**
 * FPS and resolution overlay, drawn as a tinted chip in the top-right corner in place of
 * OpenCV's default text.
 *
 * @param density display density in pixels per dp, used to keep the chip a constant physical size.
 */
class CvFpsMeter(
    private val density: Float = 1f,
) : FpsMeter() {
    private companion object {
        const val STEP = 20
        const val MARGIN_DP = 12f
        const val PADDING_H_DP = 10f
        const val PADDING_V_DP = 7f
        const val CORNER_DP = 14f
        const val FPS_TEXT_DP = 15f
        const val RES_TEXT_DP = 12f
        const val LINE_GAP_DP = 3f
        const val BACKGROUND_COLOR = 0xB3101014.toInt()
        const val FPS_COLOR = 0xFF7C6CFF.toInt()
        const val RES_COLOR = 0xB3FFFFFF.toInt()
    }

    private val fpsFormat = DecimalFormat("0.0")

    private var mFramesCounter = 0
    private var mFrequency = 0.0
    private var mprevFrameTime: Long = 0
    private var mStrfps = ""
    private var mStrRes = ""
    private var mIsInitialized = false
    private var mWidth = 0
    private var mHeight = 0

    /** Additional top offset in pixels, used to clear the status bar and display cutout. */
    var topInset: Float = 0f

    private fun dp(value: Float) = value * density

    private val backgroundPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BACKGROUND_COLOR
            style = Paint.Style.FILL
        }

    private val fpsPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = FPS_COLOR
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

    private val resPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RES_COLOR
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.RIGHT
        }

    override fun init() {
        mFramesCounter = 0
        mFrequency = Core.getTickFrequency()
        mprevFrameTime = Core.getTickCount()
        mStrfps = ""
        fpsPaint.textSize = dp(FPS_TEXT_DP)
        resPaint.textSize = dp(RES_TEXT_DP)
    }

    override fun measure() {
        if (!mIsInitialized) {
            init()
            mIsInitialized = true
        } else {
            mFramesCounter++
            if (mFramesCounter % STEP == 0) {
                val time = Core.getTickCount()
                val fps = STEP * mFrequency / (time - mprevFrameTime)
                mprevFrameTime = time
                mStrfps = "${fpsFormat.format(fps)} FPS"
            }
        }
    }

    override fun setResolution(
        width: Int,
        height: Int,
    ) {
        mWidth = width
        mHeight = height
        mStrRes = if (width != 0 && height != 0) "$width × $height" else ""
    }

    override fun draw(
        canvas: Canvas,
        offsetx: Float,
        offsety: Float,
    ) {
        if (mStrfps.isEmpty()) return

        val paddingH = dp(PADDING_H_DP)
        val paddingV = dp(PADDING_V_DP)
        val margin = dp(MARGIN_DP)
        val gap = if (mStrRes.isEmpty()) 0f else dp(LINE_GAP_DP)

        val fpsHeight = fpsPaint.textSize
        val resHeight = if (mStrRes.isEmpty()) 0f else resPaint.textSize
        val contentWidth = maxOf(fpsPaint.measureText(mStrfps), resPaint.measureText(mStrRes))
        val contentHeight = fpsHeight + gap + resHeight

        // Anchor to the top-right of the canvas, below the status bar, respecting the
        // offset OpenCV passes in.
        val right = canvas.width - margin
        val top = margin + offsety + topInset
        val left = right - contentWidth - paddingH * 2
        val bottom = top + contentHeight + paddingV * 2

        canvas.drawRoundRect(
            RectF(left, top, right, bottom),
            dp(CORNER_DP),
            dp(CORNER_DP),
            backgroundPaint,
        )

        val textRight = right - paddingH
        canvas.drawText(mStrfps, textRight, top + paddingV + fpsHeight * 0.85f, fpsPaint)
        if (mStrRes.isNotEmpty()) {
            canvas.drawText(mStrRes, textRight, top + paddingV + fpsHeight + gap + resHeight * 0.85f, resPaint)
        }
    }
}
