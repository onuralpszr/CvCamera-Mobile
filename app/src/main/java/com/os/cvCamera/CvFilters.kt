package com.os.cvCamera

import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.CvType.CV_8UC1
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY
import org.opencv.imgproc.Imgproc.GaussianBlur
import org.opencv.imgproc.Imgproc.cvtColor
import org.opencv.imgproc.Imgproc.Canny
import org.opencv.imgproc.Imgproc.Sobel


fun Mat.toSobel(): Mat {
    Sobel(this, this, CV_8UC1, 1, 0)
    return this
}

fun Mat.toSepia(): Mat {

    // multipying image with special sepia matrix
    val mSepiaKernel = Mat(4, 4, CvType.CV_32F)
    mSepiaKernel.put(0, 0,  /* R */0.189, 0.769, 0.393, 0.0)
    mSepiaKernel.put(1, 0,  /* G */0.168, 0.686, 0.349, 0.0)
    mSepiaKernel.put(2, 0,  /* B */0.131, 0.534, 0.272, 0.0)
    mSepiaKernel.put(3, 0,  /* A */0.000, 0.000, 0.000, 1.0)

    val tmpMat = Mat()
    Core.transform(this, tmpMat, mSepiaKernel)
    return tmpMat
}

fun CameraBridgeViewBase.CvCameraViewFrame.toSobel(inputMat: Mat): Mat {
    Sobel(this.gray(), inputMat, CV_8UC1, 1, 0);
    return inputMat
}

fun CameraBridgeViewBase.CvCameraViewFrame.toCanny(inputMat: Mat): Mat {
    Canny(this.rgba(), inputMat, 80.0, 90.0);
    return inputMat
}

fun Mat.toPencilSketch(): Mat {
    val grayImg = Mat()
    val invertImg = Mat()
    val blurImg = Mat()
    val invblurImg = Mat()
    val sketchImg = Mat()
    cvtColor(this, grayImg, COLOR_BGR2GRAY)
    Core.bitwise_not(grayImg, invertImg)
    GaussianBlur(invertImg, blurImg, Size(7.0, 7.0), 0.0)
    Core.bitwise_not(blurImg, invblurImg)
    Core.divide(256.0, grayImg, sketchImg)

    grayImg.release()
    invertImg.release()
    blurImg.release()
    invertImg.release()

    //pencilSketch(this,tmpMat,tmpMat1, 40f, 0.07f, 0.05f)
    return sketchImg
}