package com.os.cvcamera.java;

import java.util.ArrayList;
import java.util.List;

import com.os.cvcamera.ui.features.NamedEffect;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Effect catalogue for the Java example, written against the OpenCV Java bindings.
 *
 * These mirror the Kotlin example's effects and the C++ example's native ones, so the three apps
 * differ only in how the pixels are processed.
 *
 * Camera frames are {@code CV_8UC4} (RGBA). Effects that work on fewer channels convert into a
 * temporary and write the result back, since OpenCV rejects an in place conversion.
 */
final class JavaEffects {

    private JavaEffects() {
    }

    /** Catalogue in display order. The first entry is the no-op. */
    static List<NamedEffect> all() {
        List<NamedEffect> effects = new ArrayList<>();
        effects.add(new NamedEffect(com.os.cvcamera.ui.R.string.effect_none, frame -> frame));
        effects.add(new NamedEffect(com.os.cvcamera.ui.R.string.effect_greyscale, JavaEffects::greyscale));
        effects.add(new NamedEffect(com.os.cvcamera.ui.R.string.effect_canny, JavaEffects::canny));
        effects.add(new NamedEffect(com.os.cvcamera.ui.R.string.effect_sobel, JavaEffects::sobel));
        effects.add(new NamedEffect(com.os.cvcamera.ui.R.string.effect_sepia, JavaEffects::sepia));
        effects.add(new NamedEffect(com.os.cvcamera.ui.R.string.effect_blur, JavaEffects::blur));
        effects.add(new NamedEffect(com.os.cvcamera.ui.R.string.effect_negative, JavaEffects::negative));
        effects.add(new NamedEffect(com.os.cvcamera.ui.R.string.effect_sharpen, JavaEffects::sharpen));
        effects.add(new NamedEffect(com.os.cvcamera.ui.R.string.effect_emboss, JavaEffects::emboss));
        effects.add(new NamedEffect(com.os.cvcamera.ui.R.string.effect_binary, JavaEffects::binary));
        effects.add(new NamedEffect(com.os.cvcamera.ui.R.string.effect_hsv, JavaEffects::hsv));
        return effects;
    }

    private static Mat greyscale(Mat rgba) {
        Mat grey = new Mat();
        Imgproc.cvtColor(rgba, grey, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.cvtColor(grey, rgba, Imgproc.COLOR_GRAY2RGBA);
        grey.release();
        return rgba;
    }

    private static Mat canny(Mat rgba) {
        Mat grey = new Mat();
        Mat edges = new Mat();
        Imgproc.cvtColor(rgba, grey, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.Canny(grey, edges, 60, 160);
        Imgproc.cvtColor(edges, rgba, Imgproc.COLOR_GRAY2RGBA);
        grey.release();
        edges.release();
        return rgba;
    }

    private static Mat sobel(Mat rgba) {
        Mat grey = new Mat();
        Mat gradient = new Mat();
        Mat absolute = new Mat();
        Imgproc.cvtColor(rgba, grey, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.Sobel(grey, gradient, CvType.CV_16S, 1, 1);
        Core.convertScaleAbs(gradient, absolute);
        Imgproc.cvtColor(absolute, rgba, Imgproc.COLOR_GRAY2RGBA);
        grey.release();
        gradient.release();
        absolute.release();
        return rgba;
    }

    private static Mat sepia(Mat rgba) {
        Mat kernel = new Mat(4, 4, CvType.CV_32F);
        kernel.put(0, 0, 0.393, 0.769, 0.189, 0.0);
        kernel.put(1, 0, 0.349, 0.686, 0.168, 0.0);
        kernel.put(2, 0, 0.272, 0.534, 0.131, 0.0);
        kernel.put(3, 0, 0.0, 0.0, 0.0, 1.0);
        Core.transform(rgba, rgba, kernel);
        kernel.release();
        return rgba;
    }

    private static Mat blur(Mat rgba) {
        Imgproc.GaussianBlur(rgba, rgba, new Size(15, 15), 0);
        return rgba;
    }

    private static Mat negative(Mat rgba) {
        Core.bitwise_not(rgba, rgba);
        return rgba;
    }

    private static Mat sharpen(Mat rgba) {
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        kernel.put(0, 0, 0.0, -1.0, 0.0);
        kernel.put(1, 0, -1.0, 5.0, -1.0);
        kernel.put(2, 0, 0.0, -1.0, 0.0);
        Imgproc.filter2D(rgba, rgba, -1, kernel);
        kernel.release();
        return rgba;
    }

    private static Mat emboss(Mat rgba) {
        Mat grey = new Mat();
        Mat embossed = new Mat();
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        kernel.put(0, 0, -2.0, -1.0, 0.0);
        kernel.put(1, 0, -1.0, 1.0, 1.0);
        kernel.put(2, 0, 0.0, 1.0, 2.0);
        Imgproc.cvtColor(rgba, grey, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.filter2D(grey, embossed, -1, kernel, new org.opencv.core.Point(-1, -1), 128.0);
        Imgproc.cvtColor(embossed, rgba, Imgproc.COLOR_GRAY2RGBA);
        grey.release();
        embossed.release();
        kernel.release();
        return rgba;
    }

    private static Mat binary(Mat rgba) {
        Mat grey = new Mat();
        Mat thresholded = new Mat();
        Imgproc.cvtColor(rgba, grey, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.threshold(grey, thresholded, 127, 255, Imgproc.THRESH_BINARY);
        Imgproc.cvtColor(thresholded, rgba, Imgproc.COLOR_GRAY2RGBA);
        grey.release();
        thresholded.release();
        return rgba;
    }

    private static Mat hsv(Mat rgba) {
        Mat rgb = new Mat();
        Mat hsv = new Mat();
        Imgproc.cvtColor(rgba, rgb, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(rgb, hsv, Imgproc.COLOR_RGB2HSV);
        Imgproc.cvtColor(hsv, rgba, Imgproc.COLOR_RGB2RGBA);
        rgb.release();
        hsv.release();
        return rgba;
    }
}
