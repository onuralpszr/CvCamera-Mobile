package com.os.cvCamera.features

import android.net.Uri
import org.opencv.core.Mat

/**
 * An optional capability of the camera screen.
 *
 * Each implementation is self-contained and registered once, in the `features` list in
 * `MainActivity`. That list is the only reference to any feature, and its order defines the
 * frame-processing order.
 *
 * Implementations are constructed before the camera is running. [attach] runs once the view
 * hierarchy exists; menu and frame callbacks arrive after that.
 *
 * Camera internals that OpenCV declares `protected` (`mCaptureSession`, `mPreviewRequestBuilder`,
 * `mScale`, `mFpsMeter`) are reachable only from a `JavaCamera2View` subclass. That plumbing
 * therefore lives in `ExtendJavaCamera2View`, which exposes it as public API for features to use.
 */
interface CameraFeature {
    /**
     * Called once after the activity's views are ready. Use it to register listeners, seed
     * initial UI state, or add overlay views.
     */
    fun attach() = Unit

    /** Called when the screen becomes interactive. Pair with [onPause]. */
    fun onResume() = Unit

    /** Called when the screen stops being interactive. Release sensors and animations here. */
    fun onPause() = Unit

    /** Called from `onDestroy`. Release any lasting resources such as audio or overlay views. */
    fun detach() = Unit

    /**
     * Handle a bottom bar menu click.
     *
     * @return true when this feature owns [itemId] and has handled it, so the dispatcher can
     *   stop asking the remaining features.
     */
    fun onMenuItemSelected(itemId: Int): Boolean = false

    /**
     * Transform a camera frame. Called on the camera thread for every frame, in registration
     * order, so the `features` list also defines the processing pipeline.
     *
     * Return [frame] unchanged for features that do not touch pixels.
     */
    fun processFrame(frame: Mat): Mat = frame

    /** The front/back camera was swapped. Reset any state tied to the previous camera. */
    fun onCameraSwitched() = Unit

    /**
     * A capture was requested and the frame is about to be encoded. Called on the camera thread,
     * so this suits shutter feedback that must line up with the frame actually saved.
     */
    fun onPhotoCaptureStarted() = Unit

    /**
     * A photo has just been written to [uri]. Called on the camera thread, after the bytes are
     * flushed, so post-processing such as EXIF tagging can run.
     */
    fun onPhotoSaved(uri: Uri) = Unit
}
