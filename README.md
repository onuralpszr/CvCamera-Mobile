
<h1 align="center">CvCamera-Mobile</h1>
<h3 align="center">OpenCV's Android Camera2 with OpenCV4 (JavaCamera2View)</h3>

<p align="center">
    <a href="https://opensource.org/license/mit/"><img alt="MIT" src="https://img.shields.io/badge/License-MIT-yellow?logo=MIT&logoColor=white"></a>
    <a href="https://kotlinlang.org/"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-1.8.22-%23E34F26?logo=Kotlin&logoColor=white"></a>
    <a href="https://gradle.org/releases/"><img alt="Gradle" src="https://img.shields.io/badge/Gradle-8.1.1-02303A?logo=Gradle&logoColor=white"></a>
    <a href="https://opencv.org/"><img alt="OpenCV" src="https://img.shields.io/badge/OpenCV-4.8.1-5C3EE8?logo=OpenCV&logoColor=white"></a>
    <a href="https://conventionalcommits.org"><img alt="Conventional Commits" src="https://img.shields.io/badge/Conventional%20Commit-1.0.0-FE5196?logo=conventionalcommits&logoColor=white"></a>
    <a href="https://developer.android.com/studio"><img alt="android-studio" src="https://img.shields.io/badge/android studio-Giraffe-3DDC84?logo=androidstudio&logoColor=white"></a>
    <a href="https://github.com/onuralpszr/CvCamera-Mobile/actions/workflows/android-ci.yml"><img alt="GithubCI" src="https://github.com/onuralpszr/CvCamera-Mobile/actions/workflows/android-ci.yml/badge.svg?branch=main"></a>
    <a href="https://results.pre-commit.ci/latest/github/onuralpszr/CvCamera-Mobile/main"><img alt="pre-commit.ci status" src="https://results.pre-commit.ci/badge/github/onuralpszr/CvCamera-Mobile/main.svg"></a>
    <a href="https://snyk.io/test/github/onuralpszr/CvCamera-Mobile"><img alt="snyk-security" src="https://snyk.io/test/github/onuralpszr/CvCamera-Mobile/badge.svg"></a>
    <a href="https://www.codefactor.io/repository/github/onuralpszr/cvcamera-mobile/overview/main"><img src="https://www.codefactor.io/repository/github/onuralpszr/cvcamera-mobile/badge/main" alt="CodeFactor" /></a>
</p>





 <p align="center">
    <img src="appPreview/appOverview.png" width="200" max-height="%20"/>
    <img src="appPreview/appOverview2.png" width="200" max-height="%20"/>
</p>


This android project is barebone setting up OpenCV 4.8.1 (and other 4.x.y versions) for Android in [Android Studio](https://developer.android.com/studio) with Native Development Kit (NDK) support.
[Android NDK](https://developer.android.com/ndk) enables you to implement your [OpenCV](https://opencv.org) image processing pipeline in C++ and call that C++ code from Android Kotlin/Java code through JNI ([Java Native Interface](https://en.wikipedia.org/wiki/Java_Native_Interface)).

This sample Android application displays a live camera feed only and camera switch to back and front in runtime.


## How to use this repository

1. [Download and Install Android Studio](https://developer.android.com/studio)

2. [Install NDK and CMake](https://developer.android.com/studio/projects/install-ndk.md)

3. Clone this repository as an Android Studio project :
     * In Android Studio, click on `File -> New -> Project from Version Control -> Git`
     * Paste this repository *Github URL*, choose a *project directory* and click next.

4. Install *OpenCV Android release* :
    * Download [OpenCV 4.8.1 Android release](https://github.com/opencv/opencv/releases/download/4.8.1/opencv-4.8.1-android-sdk.zip) or download latest available Android release on [OpenCV website](https://opencv.org/releases/).
    * Unzip downloaded file and put **OpenCV-android-sdk** directory next your project and rename folder `opencvsdk481`. If you want to place somewhere else please change path in `settings.gradle`

    * Optional(For linux) you can run setupOpenCV.sh for automatic download and setup gradle file for opencv


5. Sync Gradle and run the application on your Android Device!

## Keywords

Kotlin, OpenCV 4, Android, Android Studio, Native, NDK, Native Development Kit, JNI, C++,
