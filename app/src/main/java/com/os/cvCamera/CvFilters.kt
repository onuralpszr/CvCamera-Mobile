package com.os.cvCamera

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.CvType.CV_8UC1
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfRect
import org.opencv.core.Scalar
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
        0.189,
        0.769,
        0.393,
        0.0,
        0.168,
        0.686,
        0.349,
        0.0,
        0.131,
        0.534,
        0.272,
        0.0,
        0.0,
        0.0,
        0.0,
        1.0,
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

/**
 * Negative/Invert filter - inverts all colors
 */
fun Mat.toNegative(): Mat {
    Core.bitwise_not(this, this)
    return this
}

/**
 * Sharpen filter using convolution kernel
 */
fun Mat.toSharpen(): Mat {
    val kernel = Mat(3, 3, CvType.CV_32F)
    kernel.put(
        0,
        0,
        0.0,
        -1.0,
        0.0,
        -1.0,
        5.0,
        -1.0,
        0.0,
        -1.0,
        0.0,
    )
    Imgproc.filter2D(this, this, -1, kernel)
    kernel.release()
    return this
}

/**
 * Emboss effect using convolution kernel
 */
fun Mat.toEmboss(): Mat {
    val kernel = Mat(3, 3, CvType.CV_32F)
    kernel.put(
        0,
        0,
        -2.0,
        -1.0,
        0.0,
        -1.0,
        1.0,
        1.0,
        0.0,
        1.0,
        2.0,
    )
    Imgproc.filter2D(this, this, -1, kernel)
    kernel.release()
    return this
}

/**
 * Cartoon effect using bilateral filter and edge detection
 */
fun Mat.toCartoon(): Mat {
    val gray = Mat()
    val edges = Mat()
    val color = Mat()

    // Convert to grayscale
    cvtColor(this, gray, COLOR_BGR2GRAY)

    // Apply median blur to reduce noise
    Imgproc.medianBlur(gray, gray, 7)

    // Detect edges using adaptive threshold
    Imgproc.adaptiveThreshold(
        gray,
        edges,
        255.0,
        Imgproc.ADAPTIVE_THRESH_MEAN_C,
        Imgproc.THRESH_BINARY,
        9,
        9.0,
    )

    // Apply bilateral filter for color smoothing
    Imgproc.bilateralFilter(this, color, 9, 300.0, 300.0)

    // Convert edges to color
    val edgesColor = Mat()
    cvtColor(edges, edgesColor, Imgproc.COLOR_GRAY2RGBA)

    // Combine edges with color image
    Core.bitwise_and(color, edgesColor, this)

    // Release temporary matrices
    gray.release()
    edges.release()
    color.release()
    edgesColor.release()

    return this
}

/**
 * Binary threshold filter
 */
fun Mat.toBinary(): Mat {
    val gray = Mat()
    cvtColor(this, gray, COLOR_BGR2GRAY)
    Imgproc.threshold(gray, this, 127.0, 255.0, Imgproc.THRESH_BINARY)
    gray.release()
    return this
}

/**
 * Posterize effect - reduces color palette
 */
fun Mat.toPosterize(levels: Int = 4): Mat {
    val div = 256 / levels
    for (i in 0 until this.rows()) {
        for (j in 0 until this.cols()) {
            val pixel = this.get(i, j)
            for (k in pixel.indices) {
                pixel[k] = (pixel[k] / div).toInt() * div.toDouble()
            }
            this.put(i, j, *pixel)
        }
    }
    return this
}

/**
 * Sketch/Pencil effect
 */
fun Mat.toSketch(): Mat {
    val gray = Mat()
    val blur = Mat()
    val sketch = Mat()

    // Convert to grayscale
    cvtColor(this, gray, COLOR_BGR2GRAY)

    // Invert
    Core.bitwise_not(gray, blur)

    // Apply Gaussian blur
    Imgproc.GaussianBlur(blur, blur, org.opencv.core.Size(21.0, 21.0), 0.0)

    // Blend using divide
    Core.divide(gray, blur, sketch, 256.0)

    // Copy result back
    sketch.copyTo(this)

    gray.release()
    blur.release()
    sketch.release()

    return this
}

/**
 * Vignette effect - darkens the edges
 */
fun Mat.toVignette(): Mat {
    val rows = this.rows()
    val cols = this.cols()

    // Create gradient mask
    val kernelX = Imgproc.getGaussianKernel(cols, cols / 2.0)
    val kernelY = Imgproc.getGaussianKernel(rows, rows / 2.0)

    val kernelXTranspose = Mat()
    Core.transpose(kernelX, kernelXTranspose)

    val kernel = Mat()
    Core.gemm(kernelY, kernelXTranspose, 1.0, Mat(), 0.0, kernel)

    // Normalize
    Core.normalize(kernel, kernel, 0.0, 1.0, Core.NORM_MINMAX)

    // Convert to proper type and apply
    val mask = Mat()
    kernel.convertTo(mask, CvType.CV_32F)

    // Split channels
    val channels = mutableListOf<Mat>()
    Core.split(this, channels)

    // Apply mask to each channel
    for (channel in channels) {
        val floatChannel = Mat()
        channel.convertTo(floatChannel, CvType.CV_32F)
        Core.multiply(floatChannel, mask, floatChannel)
        floatChannel.convertTo(channel, CvType.CV_8U)
        floatChannel.release()
    }

    // Merge channels back
    Core.merge(channels, this)

    // Release
    kernelX.release()
    kernelY.release()
    kernelXTranspose.release()
    kernel.release()
    mask.release()
    channels.forEach { it.release() }

    return this
}

/**
 * Detect contours and draw them
 */
fun Mat.toContours(): Mat {
    val gray = Mat()
    val edges = Mat()
    val hierarchy = Mat()

    cvtColor(this, gray, COLOR_BGR2GRAY)
    Imgproc.GaussianBlur(gray, gray, org.opencv.core.Size(5.0, 5.0), 0.0)
    Canny(gray, edges, 50.0, 150.0)

    val contours = mutableListOf<MatOfRect>()
    Imgproc.findContours(
        edges,
        contours as List<MatOfPoint>,
        hierarchy,
        Imgproc.RETR_TREE,
        Imgproc.CHAIN_APPROX_SIMPLE,
    )

    // Draw contours
    Imgproc.drawContours(
        this,
        contours as List<MatOfPoint>,
        -1,
        Scalar(0.0, 255.0, 0.0, 255.0),
        2,
    )

    gray.release()
    edges.release()
    hierarchy.release()

    return this
}
