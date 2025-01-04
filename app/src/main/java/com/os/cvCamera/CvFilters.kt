package com.os.cvCamera

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.CvType.CV_8UC1
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY
import org.opencv.imgproc.Imgproc.Canny
import org.opencv.imgproc.Imgproc.Sobel
import org.opencv.imgproc.Imgproc.cvtColor

fun Mat.toSobel(): Mat {
    Sobel(this, this, CV_8UC1, 1, 0)
    return this
}

fun Mat.toSepia(): Mat {
    val sepiaKernel = Mat(4, 4, CvType.CV_32F)
    sepiaKernel.put(
        0,
        0,
        0.189, 0.769, 0.393, 0.0,
        0.168, 0.686, 0.349, 0.0,
        0.131, 0.534, 0.272, 0.0,
        0.0, 0.0, 0.0, 1.0,
    )
    Core.transform(this, this, sepiaKernel)
    return this
}

fun Mat.toGray(): Mat {
    cvtColor(this, this, COLOR_BGR2GRAY)
    return this
}

fun Mat.toCanny(): Mat {
    val tmpMat = Mat()
    Canny(this, tmpMat, 80.0, 90.0)
    return tmpMat
}

fun Mat.toBlur(): Mat {
    Imgproc.GaussianBlur(this, this, org.opencv.core.Size(15.0, 15.0), 0.0)
    return this
}

fun Mat.toHSV(): Mat {
    cvtColor(this, this, Imgproc.COLOR_BGR2HSV)
    return this
}

fun Mat.toEdgeDetection(): Mat {
    val edges = Mat()
    cvtColor(this, edges, COLOR_BGR2GRAY)
    Imgproc.GaussianBlur(edges, edges, org.opencv.core.Size(3.0, 3.0), 0.0)
    Imgproc.Laplacian(edges, edges, CvType.CV_8U)
    return edges
}
