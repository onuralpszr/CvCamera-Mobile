
# CvCamera-Mobile - Android Camera2 with OpenCV4

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) ![kotlin-version](https://img.shields.io/badge/kotlin-1.7.20-orange) ![opencv-version](https://img.shields.io/badge/opencvAndroid-4.7.0-green) [![Conventional Commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-%23FE5196?logo=conventionalcommits&logoColor=white)](https://conventionalcommits.org) [![android-build](https://github.com/onuralpszr/CvCamera-Mobile/actions/workflows/android-ci.yml/badge.svg?branch=main)](https://github.com/onuralpszr/CvCamera-Mobile/actions/workflows/android-ci.yml)

![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84.svg?style=for-the-badge&logo=android-studio&logoColor=white)


 <p align="center">
    <img src="appPreview/appOverview.png" width="200" max-height="%20"/>
    <img src="appPreview/appOverview2.png" width="200" max-height="%20"/>
</p>


This android project is barebone setting up OpenCV 4.7.0 (and other 4.x.y versions) for Android in [Android Studio](https://developer.android.com/studio) with Native Development Kit (NDK) support.
[Android NDK](https://developer.android.com/ndk) enables you to implement your [OpenCV](https://opencv.org) image processing pipeline in C++ and call that C++ code from Android Kotlin/Java code through JNI ([Java Native Interface](https://en.wikipedia.org/wiki/Java_Native_Interface)).

This sample Android application displays a live camera feed only and camera switch in runtime. 


## How to use this repository

1. [Download and Install Android Studio](https://developer.android.com/studio)

2. [Install NDK and CMake](https://developer.android.com/studio/projects/install-ndk.md)

3. Clone this repository as an Android Studio project :
     * In Android Studio, click on `File -> New -> Project from Version Control -> Git`
     * Paste this repository *Github URL*, choose a *project directory* and click next.

4. Install *OpenCV Android release* :
    * Download [OpenCV 4.7.0 Android release](https://sourceforge.net/projects/opencvlibrary/files/4.7.0/opencv-4.7.0-android-sdk.zip/download) or download latest available Android release on [OpenCV website](https://opencv.org/releases/).
    * Unzip downloaded file and put **OpenCV-android-sdk** directory next your project and rename folder `opencvsdk460`. If you want to place somewhere else please change path in `settings.gradle` 

    * Optional(For linux) you can run setupOpenCV.sh for automatic download and setup gradle file for opencv 


5. Sync Gradle and run the application on your Android Device!

## Keywords

Kotlin, OpenCV 4, Android, Android Studio, Native, NDK, Native Development Kit, JNI, C++,
