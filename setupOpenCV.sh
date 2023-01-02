#!/usr/bin/bash

wget -O opencv-4.7.0-android-sdk.zip https://github.com/opencv/opencv/releases/download/4.7.0/opencv-4.7.0-android-sdk.zip
unzip -qq opencv-4.7.0-android-sdk.zip
unzip -qq OpenCV4Android.zip
mv OpenCV-android-sdk opencvsdk470
sudo sed -i 's/compileSdkVersion 26/compileSdkVersion 33/g' opencvsdk470/sdk/build.gradle
sudo sed -i 's/targetSdkVersion 26/targetSdkVersion 33/g' opencvsdk470/sdk/build.gradle