package com.os.cvcamera.ndk

import com.os.cvcamera.ui.features.NamedEffect
import org.opencv.core.Mat
import com.os.cvcamera.ui.R as UiR

/**
 * JNI bridge to the effects implemented in `native-lib.cpp`.
 *
 * Only the Mat address is passed across, so frames are processed in place without copying pixel
 * data. Each effect mirrors one from the Kotlin example, which uses the OpenCV Java bindings for
 * the same work.
 */
object NativeEffects {
    init {
        // OpenCV's shared library has to be loaded before the library that links against it.
        System.loadLibrary("opencv_java4")
        System.loadLibrary("cvcamerandk")
    }

    /** Version reported by the native OpenCV library, which proves the JNI path is linked. */
    external fun openCvVersion(): String

    private external fun greyscale(addr: Long)

    private external fun canny(
        addr: Long,
        low: Double,
        high: Double,
    )

    private external fun sobel(addr: Long)

    private external fun sepia(addr: Long)

    private external fun blur(
        addr: Long,
        kernel: Int,
    )

    private external fun negative(addr: Long)

    private external fun sharpen(addr: Long)

    private external fun emboss(addr: Long)

    private external fun binary(
        addr: Long,
        threshold: Double,
    )

    private external fun cartoon(addr: Long)

    /** Catalogue offered by the effect picker, in display order. */
    fun all(): List<NamedEffect> =
        listOf(
            NamedEffect(UiR.string.effect_none) { it },
            NamedEffect(UiR.string.effect_greyscale) { frame -> frame.also { greyscale(it.nativeObjAddr) } },
            NamedEffect(UiR.string.effect_canny) { frame -> frame.also { canny(it.nativeObjAddr, 60.0, 160.0) } },
            NamedEffect(UiR.string.effect_sobel) { frame -> frame.also { sobel(it.nativeObjAddr) } },
            NamedEffect(UiR.string.effect_sepia) { frame -> frame.also { sepia(it.nativeObjAddr) } },
            NamedEffect(UiR.string.effect_blur) { frame -> frame.also { blur(it.nativeObjAddr, 15) } },
            NamedEffect(UiR.string.effect_negative) { frame -> frame.also { negative(it.nativeObjAddr) } },
            NamedEffect(UiR.string.effect_sharpen) { frame -> frame.also { sharpen(it.nativeObjAddr) } },
            NamedEffect(UiR.string.effect_emboss) { frame -> frame.also { emboss(it.nativeObjAddr) } },
            NamedEffect(UiR.string.effect_binary) { frame -> frame.also { binary(it.nativeObjAddr, 127.0) } },
            NamedEffect(UiR.string.effect_cartoon) { frame -> frame.also { cartoon(it.nativeObjAddr) } },
        )

    private inline fun Mat.also(block: (Mat) -> Unit): Mat {
        block(this)
        return this
    }
}
