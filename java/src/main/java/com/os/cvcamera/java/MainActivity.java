package com.os.cvcamera.java;

import java.util.ArrayList;
import java.util.List;

import com.os.cvcamera.ui.CameraScreenActivity;
import com.os.cvcamera.ui.features.AboutDialog;
import com.os.cvcamera.ui.features.CameraFeature;
import com.os.cvcamera.ui.features.FilterPicker;

/**
 * Java example: the same camera screen as the other examples, with no Kotlin in this app's own
 * sources and no native code.
 *
 * The shared UI comes from the {@code camera-ui} library, so the only thing specific to this
 * example is the effect catalogue in {@link JavaEffects}.
 */
public class MainActivity extends CameraScreenActivity {

    static {
        // Must be loaded before the preview is inflated.
        System.loadLibrary("opencv_java4");
    }

    @Override
    protected List<CameraFeature> createFeatures() {
        List<CameraFeature> features = new ArrayList<>();
        features.add(new FilterPicker(this, JavaEffects.all()));
        features.add(new AboutDialog(
                this,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                getString(R.string.implementation_java),
                null,
                null));
        features.addAll(defaultFeatures());
        return features;
    }
}
