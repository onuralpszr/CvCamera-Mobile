package com.os.cvcamera.java;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * OpenCV example written entirely in Java, with no Kotlin and no native code.
 *
 * Frames are processed through the OpenCV Java bindings. Tapping the preview cycles the effect.
 *
 * This is the counterpart to the Kotlin example in {@code app/} and the C++ example in
 * {@code cpp/}.
 */
public class MainActivity extends CameraActivity implements CvCameraViewListener2 {

    private static final String TAG = "CvCameraJava";

    static {
        System.loadLibrary("opencv_java4");
    }

    /** Effect applied to each frame. */
    private enum Mode {
        NONE,
        GREYSCALE,
        CANNY
    }

    private CameraBridgeViewBase cameraView;
    private Mode mode = Mode.CANNY;

    /** Reused across frames so a new Mat is not allocated per frame. */
    private Mat grey;
    private Mat edges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "OpenCV version: " + OpenCVLoader.OPENCV_VERSION);

        cameraView = findViewById(R.id.cameraView);
        cameraView.setCvCameraViewListener(this);
        cameraView.setOnClickListener(view -> cycleMode());
    }

    private void cycleMode() {
        switch (mode) {
            case CANNY:
                mode = Mode.GREYSCALE;
                break;
            case GREYSCALE:
                mode = Mode.NONE;
                break;
            default:
                mode = Mode.CANNY;
                break;
        }
        Toast.makeText(this, getString(R.string.mode_changed, mode.name()), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraView);
    }

    @Override
    protected void onCameraPermissionGranted() {
        super.onCameraPermissionGranted();
        cameraView.enableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        grey = new Mat();
        edges = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        if (grey != null) {
            grey.release();
        }
        if (edges != null) {
            edges.release();
        }
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        switch (mode) {
            case CANNY:
                // Camera frames are CV_8UC4; RGBA2GRAY accepts the alpha channel.
                Imgproc.cvtColor(rgba, grey, Imgproc.COLOR_RGBA2GRAY);
                Imgproc.Canny(grey, edges, 60, 160);
                Imgproc.cvtColor(edges, rgba, Imgproc.COLOR_GRAY2RGBA);
                break;
            case GREYSCALE:
                Imgproc.cvtColor(rgba, grey, Imgproc.COLOR_RGBA2GRAY);
                Imgproc.cvtColor(grey, rgba, Imgproc.COLOR_GRAY2RGBA);
                break;
            default:
                break;
        }
        return rgba;
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.disableView();
    }
}
