#!/usr/bin/env sh

wget -O opencv-4.8.1-android-sdk.zip https://github.com/opencv/opencv/releases/download/4.8.1/opencv-4.8.1-android-sdk.zip
unzip -qq opencv-4.8.1-android-sdk.zip
mv OpenCV-android-sdk opencvsdk481

# Apply OpenCV Build.gradle patch
# Sudo because of Github action
sudo patch opencvsdk481/sdk/build.gradle patches/cv_build_gradle.patch
