#!/usr/bin/bash

wget -O opencv-4.8.0-android-sdk.zip https://github.com/opencv/opencv/releases/download/4.8.0/opencv-4.8.0-android-sdk.zip
unzip -qq opencv-4.8.0-android-sdk.zip
mv OpenCV-android-sdk opencvsdk480

# Apply OpenCV Build.gradle patch
sudo patch opencvsdk480/sdk/build.gradle patches/cv_build_gradle.patch
