package com.al.cvcamera


import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.Window
import android.view.WindowManager
import com.al.cvcamera.databinding.ActivityMainBinding
import org.opencv.android.*
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


class MainActivity : CameraActivity() ,CameraBridgeViewBase.CvCameraViewListener2 {

    val TAG: String = javaClass.simpleName
    private lateinit var binding: ActivityMainBinding
    private lateinit var mOpenCvCameraView: JavaCamera2View
    private lateinit var mRGBA:Mat
    private lateinit var mRGBAT:Mat

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    mOpenCvCameraView.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //OpenCV Camera
        mOpenCvCameraView = binding.CvCamera
        Log.d(TAG,"CvCameraLoaded")
        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView.setCameraIndex(1)
        mOpenCvCameraView.setCvCameraViewListener(this)
        mOpenCvCameraView.setCameraPermissionGranted()

    }

    //external fun stringFromJNI(): String

    companion object {
        // Used to load the 'cvcamera' library on application startup.
        init {
            System.loadLibrary("cvcamera")
            System.loadLibrary("opencv_java4")
        }
    }



    override fun onCameraViewStarted(width: Int, height: Int) {
        mRGBA = Mat(height,width,CvType.CV_8UC4)
        mRGBAT = Mat()

    }

    override fun onCameraViewStopped() {
        mRGBA.release()
        mRGBAT.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {

        if (inputFrame != null) {
            mRGBA = inputFrame.rgba()
        }
        Core.flip(mRGBA,mRGBAT,-1)
        Imgproc.resize(mRGBAT, mRGBAT, mRGBA.size())
        return mRGBA
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {
        super.onPointerCaptureChanged(hasCapture)
    }

    override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView.disableView()
    }

    override fun onPause() {
        super.onPause()
        mOpenCvCameraView.disableView()
    }

    override fun onResume() {
        super.onResume()

        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV loaded")
            mLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS)
        }
        else
        {
            Log.d(TAG, "OpenCV didn't load")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION,this,mLoaderCallback)
        }
        //mOpenCvCameraView.enableView()
    }
}
