#!/usr/bin/bash

wget -O opencv-4.6.0-android-sdk.zip https://sourceforge.net/projects/opencvlibrary/files/4.6.0/opencv-4.6.0-android-sdk.zip/download
unzip -qq opencv-4.6.0-android-sdk.zip
mv OpenCV-android-sdk opencvsdk460
sudo sed -i 's/compileSdkVersion 26/compileSdkVersion 33/g' opencvsdk460/sdk/build.gradle
sudo sed -i 's/targetSdkVersion 26/targetSdkVersion 33/g' opencvsdk460/sdk/build.gradle