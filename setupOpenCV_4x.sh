#!/usr/bin/env sh

wget -O opencv-4.13.0-android-sdk.zip https://github.com/opencv/opencv/releases/download/4.13.0/opencv-4.13.0-android-sdk.zip
unzip -qq opencv-4.13.0-android-sdk.zip
mv OpenCV-android-sdk opencvsdk4130

# Apply OpenCV Build.gradle + Manifest patches and copy the lint baseline.
# (Same steps in CI and locally; the previous GITHUB_ACTIONS if/else branches
#  were identical, so the conditional has been removed.)
echo "Patching OpenCV Build.gradle and Manifest"
patch opencvsdk4130/sdk/build.gradle patches/cv_build_gradle_4x.diff
patch opencvsdk4130/sdk/java/AndroidManifest.xml patches/manifest_lint.diff
echo "Copying OpenCV Lint Baseline"
cp patches/opencv-lint-baseline.xml opencvsdk4130/sdk/opencv-lint-baseline.xml
# patch opencvsdk4130/sdk/java/src/org/opencv/core/MatAt.kt patches/cv_matat_kt_old_patch.diff
