#!/usr/bin/bash

wget -O opencv-4.7.0-android-sdk.zip https://github.com/opencv/opencv/releases/download/4.7.0/opencv-4.7.0-android-sdk.zip
unzip -qq opencv-4.7.0-android-sdk.zip
mv OpenCV-android-sdk opencvsdk470

# Apply OpenCV Build.gradle patch
sudo patch opencvsdk470/sdk/build.gradle patches/cv_build_gradle.patch

