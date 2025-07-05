#!/usr/bin/env sh

wget -O opencv-4.12.0-android-sdk.zip https://github.com/opencv/opencv/releases/download/4.12.0/opencv-4.12.0-android-sdk.zip
unzip -qq opencv-4.12.0-android-sdk.zip
mv OpenCV-android-sdk opencvsdk4120

# Apply OpenCV Build.gradle and Manifest patch
# ENVIRONMENT VARIABLES check for Github Action CI exist do patching
if [ -n "$GITHUB_ACTIONS" ]; then
    echo "GITHUB_ACTIONS is set"
    echo "Patching OpenCV Build.gradle and Manifest"
    sudo patch opencvsdk4120/sdk/build.gradle patches/cv_build_gradle_4x.patch
    sudo patch opencvsdk4120/sdk/java/AndroidManifest.xml patches/manifest_lint.patch
    # sudo patch opencvsdk4120/sdk/java/src/org/opencv/core/MatAt.kt patches/cv_matat_kt.patch
else
    echo "GITHUB_ACTIONS is not set"
    echo "Patching OpenCV Build.gradle and Manifest"
    patch opencvsdk4120/sdk/build.gradle patches/cv_build_gradle_4x.patch
    patch opencvsdk4120/sdk/java/AndroidManifest.xml patches/manifest_lint.patch
    # patch opencvsdk4120/sdk/java/src/org/opencv/core/MatAt.kt patches/cv_matat_kt.patch
fi
