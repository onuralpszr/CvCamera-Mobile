package com.os.cvCamera

import android.os.Bundle
import android.view.WindowManager
import com.os.cvCamera.databinding.ActivityMainBinding
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT
import org.opencv.android.CameraBridgeViewBase.CAMERA_ID_BACK
import org.opencv.android.OpenCVLoader
import org.opencv.android.OpenCVLoader.OPENCV_VERSION
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import timber.log.Timber

class MainActivity : CameraActivity(), CvCameraViewListener2 {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mRGBA: Mat
    private lateinit var mRGBAT: Mat
    private var mCameraId: Int = CAMERA_ID_BACK

    companion object {
        init {
            System.loadLibrary("opencv_java4")
        }
    }

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Timber.d("OpenCV loaded successfully")
                    Timber.d("OpenCV Version: $OPENCV_VERSION")
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
        binding.CvCamera.setCameraIndex(CAMERA_ID_BACK)
        binding.CvCamera.setCvCameraViewListener(this)
        binding.CvCamera.setCameraPermissionGranted()
        Timber.d("OpenCV Camera Loaded")
    }


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
                inputFrame.rgba()
            } else {
                mRGBA = inputFrame.rgba()
                // flipping to show portrait mode properly
                Core.flip(mRGBA, mRGBAT, 1)
                // release the matrix to avoid memory leaks
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
        Timber.d("onDestroy")
        super.onDestroy()
        binding.CvCamera.disableView()
    }

    override fun onPause() {
        Timber.d("onPause")
        super.onPause()
        binding.CvCamera.disableView()
    }

    override fun onResume() {
        Timber.d("onResume")
        super.onResume()
        if (OpenCVLoader.initDebug()) {
            Timber.d("OpenCV loaded")
            mLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS)
        } else {
            Timber.d("OpenCV didn't load")
            OpenCVLoader.initAsync(OPENCV_VERSION, this, mLoaderCallback)
        }
    }
}
