#!/usr/bin/env sh

wget -O opencv-4.12.0-android-sdk.zip https://github.com/opencv/opencv/releases/download/4.12.0/opencv-4.12.0-android-sdk.zip
unzip -qq opencv-4.12.0-android-sdk.zip
mv OpenCV-android-sdk opencvsdk4120

# Apply OpenCV Build.gradle and Manifest patch
# ENVIRONMENT VARIABLES check for Github Action CI exist do patching
if [ -n "$GITHUB_ACTIONS" ]; then
    echo "GITHUB_ACTIONS is set"
    echo "Patching OpenCV Build.gradle and Manifest"
    sudo patch opencvsdk4120/sdk/build.gradle patches/cv_build_gradle_4x.diff
    sudo patch opencvsdk4120/sdk/java/AndroidManifest.xml patches/manifest_lint.diff
    echo "Copying OpenCV Lint Baseline"
    sudo cp patches/opencv-lint-baseline.xml opencvsdk4120/sdk/opencv-lint-baseline.xml
    # sudo patch opencvsdk4120/sdk/java/src/org/opencv/core/MatAt.kt patches/cv_matat_kt.diff
else
    echo "GITHUB_ACTIONS is not set"
    echo "Patching OpenCV Build.gradle and Manifest"
    patch opencvsdk4120/sdk/build.gradle patches/cv_build_gradle_4x.diff
    patch opencvsdk4120/sdk/java/AndroidManifest.xml patches/manifest_lint.diff
    echo "Copying OpenCV Lint Baseline"
    sudo cp patches/opencv-lint-baseline.xml opencvsdk4120/sdk/opencv-lint-baseline.xml
    # patch opencvsdk4120/sdk/java/src/org/opencv/core/MatAt.kt patches/cv_matat_kt.diff
fi
