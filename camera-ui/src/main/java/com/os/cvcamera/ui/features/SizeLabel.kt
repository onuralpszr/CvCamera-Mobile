package com.os.cvcamera.ui.features

/**
 * Formatting for camera frame sizes shown in the resolution picker.
 *
 * Takes plain dimensions rather than `android.util.Size` so the arithmetic stays free of the
 * Android framework and can be unit tested on the host JVM.
 */
internal object SizeLabel {
    /** For example `1920 × 1080  ·  16:9  ·  2.1 MP`. */
    fun describe(
        width: Int,
        height: Int,
    ): String = "$width × $height  ·  ${aspectRatio(width, height)}  ·  ${megaPixels(width, height)}"

    /** Simplified `w:h` ratio, for example `16:9` for 1920x1080. */
    fun aspectRatio(
        width: Int,
        height: Int,
    ): String {
        val divisor = greatestCommonDivisor(width, height)
        return "${width / divisor}:${height / divisor}"
    }

    /** Pixel count in megapixels to one decimal place, for example `2.1 MP`. */
    fun megaPixels(
        width: Int,
        height: Int,
    ): String {
        // Multiply as Long: large sensors such as 4080x3060 are fine here but the habit matters.
        val value = width.toLong() * height / 1_000_000.0
        return "%.1f MP".format(value)
    }

    /** Returns 1 rather than 0 for a zero size, so callers never divide by zero. */
    tailrec fun greatestCommonDivisor(
        a: Int,
        b: Int,
    ): Int = if (b == 0) (if (a == 0) 1 else a) else greatestCommonDivisor(b, a % b)
}
