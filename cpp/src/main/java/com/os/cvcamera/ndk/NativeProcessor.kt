package com.os.cvcamera.ndk

import org.opencv.core.Mat

/**
 * JNI entry points for the native frame processing in `native-lib.cpp`.
 *
 * Only the Mat address is passed across the boundary, so no pixel data is copied.
 */
object NativeProcessor {
    init {
        // OpenCV's own shared library has to be loaded before anything that links against it.
        System.loadLibrary("opencv_java4")
        System.loadLibrary("cvcamerandk")
    }

    /** Version reported by the native OpenCV library. */
    external fun openCvVersion(): String

    /** Canny edge detection, written back into [mat]. */
    external fun cannyEdges(
        matAddr: Long,
        lowThreshold: Double,
        highThreshold: Double,
    )

    /** Greyscale conversion, written back into [mat]. */
    external fun greyscale(matAddr: Long)

    fun cannyEdges(
        mat: Mat,
        lowThreshold: Double = 60.0,
        highThreshold: Double = 160.0,
    ) = cannyEdges(mat.nativeObjAddr, lowThreshold, highThreshold)

    fun greyscale(mat: Mat) = greyscale(mat.nativeObjAddr)
}
