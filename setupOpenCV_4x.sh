#!/usr/bin/env sh
# Download the OpenCV 4.x Android SDK and prepare it as a Gradle module.
#
# Produces ./opencvsdk4140, referenced by settings.gradle.kts and app/build.gradle.kts.
# Safe to re-run: an already-prepared SDK is left untouched.
set -eu

OPENCV_VERSION="4.14.0"
OPENCV_SDK_DIR="opencvsdk4140"
OPENCV_ZIP="opencv-${OPENCV_VERSION}-android-sdk.zip"
OPENCV_URL="https://github.com/opencv/opencv/releases/download/${OPENCV_VERSION}/${OPENCV_ZIP}"
OPENCV_SHA256="e8cfaf2e51f2e2127a6ede91718d1ef7587f8b6e62db922816e7c33a1f1116a7"

if [ -d "${OPENCV_SDK_DIR}/sdk" ]; then
    echo "==> ${OPENCV_SDK_DIR} already exists, nothing to do."
    echo "    Remove it and re-run this script to start from a clean SDK."
    exit 0
fi

if [ ! -f "${OPENCV_ZIP}" ]; then
    echo "==> Downloading OpenCV ${OPENCV_VERSION} Android SDK"
    wget -q --show-progress -O "${OPENCV_ZIP}" "${OPENCV_URL}"
fi

echo "==> Verifying checksum"
echo "${OPENCV_SHA256}  ${OPENCV_ZIP}" | sha256sum -c -

echo "==> Extracting"
unzip -qq "${OPENCV_ZIP}"
mv OpenCV-android-sdk "${OPENCV_SDK_DIR}"

echo "==> Patching build.gradle and AndroidManifest.xml"
patch "${OPENCV_SDK_DIR}/sdk/build.gradle" patches/cv_build_gradle_4x.diff
patch "${OPENCV_SDK_DIR}/sdk/java/AndroidManifest.xml" patches/manifest_lint.diff

echo "==> Copying lint baseline"
cp patches/opencv-lint-baseline.xml "${OPENCV_SDK_DIR}/sdk/opencv-lint-baseline.xml"

echo "==> OpenCV ${OPENCV_VERSION} ready in ${OPENCV_SDK_DIR}"
