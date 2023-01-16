package com.os.cvCamera

import org.opencv.core.CvType.CV_8UC1
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


fun Mat.toSobel(): Mat {
    Imgproc.Sobel(this, this, CV_8UC1, 1, 0)
    return this
}
