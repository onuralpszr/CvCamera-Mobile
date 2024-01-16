#!/usr/bin/env sh

wget -O opencv-4.9.0-android-sdk.zip https://github.com/opencv/opencv/releases/download/4.9.0/opencv-4.9.0-android-sdk.zip
unzip -qq opencv-4.9.0-android-sdk.zip
mv OpenCV-android-sdk opencvsdk490

# Apply OpenCV Build.gradle and Manifest patch
# ENVIRONMENT VARIABLES check for Github Action CI exist do patching
if [ -n "$GITHUB_ACTIONS" ]; then
    echo "GITHUB_ACTIONS is set"
    echo "Patching OpenCV Build.gradle and Manifest"
    sudo patch opencvsdk490/sdk/build.gradle patches/cv_build_gradle.patch
    sudo patch opencvsdk490/sdk/java/AndroidManifest.xml patches/manifest_lint.patch
else
    echo "GITHUB_ACTIONS is not set"
    echo "Patching OpenCV Build.gradle and Manifest"
    patch opencvsdk490/sdk/build.gradle patches/cv_build_gradle.patch
    patch opencvsdk490/sdk/java/AndroidManifest.xml patches/manifest_lint.patch
fi
