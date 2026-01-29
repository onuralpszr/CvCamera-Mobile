package com.os.cvCamera

import android.content.Context
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class FaceDetector(
    private val context: Context,
) {
    private var faceCascade: CascadeClassifier? = null

    init {
        loadCascade()
    }

    private fun loadCascade() {
        try {
            val inputStream = context.resources.openRawResource(R.raw.haarcascade_frontalface_default)
            val cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE)
            val cascadeFile = File(cascadeDir, "haarcascade_frontalface_default.xml")

            val outputStream = FileOutputStream(cascadeFile)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            inputStream.close()
            outputStream.close()

            faceCascade = CascadeClassifier(cascadeFile.absolutePath)
            if (faceCascade?.empty() == true) {
                Timber.e("Failed to load cascade classifier")
                faceCascade = null
            } else {
                Timber.d("Face cascade loaded successfully")
            }

            cascadeDir.delete()
        } catch (e: Exception) {
            Timber.e("Error loading cascade: ${e.message}")
            faceCascade = null
        }
    }

    fun detect(frame: Mat): Mat {
        val cascade = faceCascade ?: return frame
        if (cascade.empty()) return frame

        val gray = Mat()
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY)

        val faces = MatOfRect()
        cascade.detectMultiScale(
            gray,
            faces,
            1.1,
            3,
            0,
            Size(30.0, 30.0),
            Size(),
        )

        // Draw rectangles around detected faces
        for (rect in faces.toArray()) {
            Imgproc.rectangle(
                frame,
                Point(rect.x.toDouble(), rect.y.toDouble()),
                Point((rect.x + rect.width).toDouble(), (rect.y + rect.height).toDouble()),
                Scalar(0.0, 255.0, 0.0, 255.0),
                3,
            )
        }

        gray.release()
        faces.release()

        return frame
    }
}
