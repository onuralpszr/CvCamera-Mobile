package com.os.cvCamera

import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.CvType.CV_8UC1
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


fun Mat.toSobel(): Mat {
    Imgproc.Sobel(this, this, CV_8UC1, 1, 0)
    return this
}

fun CameraBridgeViewBase.CvCameraViewFrame.toSobel(inputMat: Mat): Mat {
    Imgproc.Sobel(this.gray(), inputMat, CV_8UC1, 1, 0);
    return inputMat
}

fun Mat.toSepia(): Mat {
    var mSepiaKernel = Mat(4, 4, CvType.CV_32F)
    mSepiaKernel.put(0, 0,  /* R */0.189, 0.769, 0.393, 0.0)
    mSepiaKernel.put(1, 0,  /* G */0.168, 0.686, 0.349, 0.0)
    mSepiaKernel.put(2, 0,  /* B */0.131, 0.534, 0.272, 0.0)
    mSepiaKernel.put(3, 0,  /* A */0.000, 0.000, 0.000, 1.0)
    // multipying image with special sepia matrix
    val tmpMat: Mat = Mat()
    Core.transform(this, tmpMat, mSepiaKernel)
    return tmpMat
}