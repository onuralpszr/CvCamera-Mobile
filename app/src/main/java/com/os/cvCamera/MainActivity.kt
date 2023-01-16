package com.os.cvCamera

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import com.os.cvCamera.databinding.ActivityMainBinding
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase.*
import org.opencv.android.OpenCVLoader
import org.opencv.android.OpenCVLoader.OPENCV_VERSION
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size

class MainActivity : CameraActivity(), CvCameraViewListener2 {

    val TAG: String = javaClass.simpleName
    private lateinit var binding: ActivityMainBinding
    private lateinit var mRGBA: Mat
    private lateinit var mRGBAT: Mat
    private var mCameraId: Int = CAMERA_ID_BACK

    companion object {
        init {
            System.loadLibrary("cvcamera")
            System.loadLibrary("opencv_java4")
        }
    }

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.d(TAG, "OpenCV loaded successfully")
                    Log.d(TAG, "OpenCV Version: $OPENCV_VERSION")
                    binding.CvCamera.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadOpenCVConfigs()

        binding.cvCameraChangeFab.setOnClickListener {
            cameraSwitch()
        }
    }

    private fun cameraSwitch() {
        mCameraId = if (mCameraId == CAMERA_ID_BACK) {
            CAMERA_ID_FRONT
        } else {
            CAMERA_ID_BACK
        }

        binding.CvCamera.disableView()
        binding.CvCamera.setCameraIndex(mCameraId)
        binding.CvCamera.enableView()

    }

    private fun loadOpenCVConfigs() {
        //OpenCV Camera
        Log.d(TAG, "CvCameraLoaded")
        binding.CvCamera.setCameraIndex(CAMERA_ID_BACK)
        binding.CvCamera.setCvCameraViewListener(this)
        binding.CvCamera.setCameraPermissionGranted()
    }

    //external fun stringFromJNI(): String

    override fun onCameraViewStarted(width: Int, height: Int) {
        mRGBA = Mat(height, width, CvType.CV_8UC4)
        mRGBAT = Mat()
    }

    override fun onCameraViewStopped() {
        mRGBA.release()
        mRGBAT.release()
    }

    override fun onCameraFrame(inputFrame: CvCameraViewFrame?): Mat {
        return if (inputFrame != null) {

            // Frame information
            val rgba = inputFrame.rgba()
            val sizeRgba: Size = rgba.size()

            val rows = sizeRgba.height.toInt()
            val cols = sizeRgba.width.toInt()

            val left = cols / 8
            val top = rows / 8

            val width = cols * 3 / 4
            val height = rows * 3 / 4

            if (mCameraId == CAMERA_ID_BACK) {
                inputFrame.toCanny(mRGBA)
            } else {
                mRGBA = inputFrame.rgba()
                // flipping to show portrait mode properly
                Core.flip(mRGBA, mRGBAT, 1)
                // releasing what's not anymore needed
                mRGBA.release()
                mRGBAT
            }
        } else {
            mRGBA
        }
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {
        super.onPointerCaptureChanged(hasCapture)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.CvCamera.disableView()
    }

    override fun onPause() {
        super.onPause()
        binding.CvCamera.disableView()
    }

    override fun onResume() {
        super.onResume()
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV loaded")
            mLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS)
        } else {
            Log.d(TAG, "OpenCV didn't load")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
        }
    }
}
