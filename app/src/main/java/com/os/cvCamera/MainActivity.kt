package com.os.cvCamera

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.TypedValue
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.get
import com.os.cvCamera.BuildConfig.GIT_HASH
import com.os.cvCamera.BuildConfig.VERSION_NAME
import com.os.cvCamera.databinding.ActivityMainBinding
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase.CAMERA_ID_BACK
import org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.OpenCVLoader
import org.opencv.android.OpenCVLoader.OPENCV_VERSION
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import timber.log.Timber

class MainActivity : CameraActivity(), CvCameraViewListener2 {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mRGBA: Mat
    private lateinit var mRGBAT: Mat
    private var mCameraId: Int = CAMERA_ID_BACK
    private var mTorchCameraId: String = ""
    private var mTorchState = false
    private lateinit var mCameraManager: CameraManager

    companion object {
        init {
            System.loadLibrary("opencv_java4")
            System.loadLibrary("cvcamera")
        }
    }

    private external fun openCVVersion(): String?

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Timber.d("OpenCV loaded successfully")
                    Timber.d("OpenCV Version: $OPENCV_VERSION")
                    binding.CvCamera.enableView()
                    binding.CvCamera.getCameraDevice()
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
        mCameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        //
        loadOpenCVConfigs()

        // Find the flashlight
        findFlashLight()

        // Load buttonConfigs
        configButtons()

        // Load button colors
        setButtonColors()

    }

    private fun setButtonColors() {
        for (i in 0..< binding.bottomAppBar.menu.size()) {
            val item = binding.bottomAppBar.menu[i]
            val typedValue = TypedValue()
            theme.resolveAttribute(android.R.attr.colorAccent, typedValue, true)
            item.icon?.colorFilter = PorterDuffColorFilter(typedValue.data, PorterDuff.Mode.SRC_ATOP)
        }
    }

    private fun configButtons() {
        binding.cvCameraChangeFab.setOnClickListener {
            cameraSwitch()
        }

        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.flashlight -> {
                    true
                }

                R.id.about -> {
                    // Get app version and githash from BuildConfig
                    val cvVer = openCVVersion() // Get OpenCV version from native code
                    val toast: Toast = Toast.makeText(
                        this,
                        "CvCamera-Mobile - Version $VERSION_NAME-$GIT_HASH - OpenCV $cvVer ",
                        Toast.LENGTH_SHORT,
                    )
                    toast.show()

                    true
                }

                else -> {
                    false
                }
            }
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
        binding.CvCamera.setCameraIndex(mCameraId)
        binding.CvCamera.setCvCameraViewListener(this)
        binding.CvCamera.setCameraPermissionGranted()
        Timber.d("OpenCV Camera Loaded")
    }

    private fun enableFlashLight() {
        mTorchState = true
        mCameraManager.setTorchMode(mTorchCameraId, true)
        Timber.d("Torch is on")
    }

    private fun findFlashLight() {
        for (cameraId in mCameraManager.cameraIdList) {
            try {
                // Check if the camera has a torchlight
                val hasTorch = mCameraManager.getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false

                if (hasTorch) {
                    // Find the ID of the camera that has a torchlight and store it in mTorchCameraId
                    Timber.d("Torch is available")
                    Timber.d("Camera Id: $cameraId")
                    mTorchCameraId = cameraId
                    mTorchState = false
                    break
                } else {
                    Timber.d("Torch is not available")
                }
            } catch (e: CameraAccessException) {
                // Handle any errors that occur while trying to access the camera
                Timber.e("CameraAccessException ${e.message}")
            }
        }
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
            if (mCameraId == CAMERA_ID_BACK) {
                inputFrame.rgba()
            } else {
                mRGBA = inputFrame.rgba()
                // Flipping to show portrait mode properly
                Core.flip(mRGBA, mRGBAT, 1)
                // Release the matrix to avoid memory leaks
                mRGBA.release()
                mRGBAT
            }
        } else {
            // return last or empty frame
            mRGBA
        }
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
