// Frame processing for the NDK example.
//
// The Kotlin side passes the address of an OpenCV Mat that already holds the camera frame, so no
// pixel data crosses the JNI boundary. Everything below runs on the camera thread.

#include <jni.h>
#include <string>

#include <android/log.h>
#include <opencv2/opencv.hpp>

#define LOG_TAG "CvCameraNdk"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {

/** Version of the OpenCV native library this app is linked against. */
JNIEXPORT jstring JNICALL
Java_com_os_cvcamera_ndk_NativeProcessor_openCvVersion(JNIEnv* env, jobject /* thiz */) {
    const std::string version = cv::getVersionString();
    LOGI("OpenCV native version: %s", version.c_str());
    return env->NewStringUTF(version.c_str());
}

/**
 * Canny edge detection, written back into the same RGBA Mat so the preview shows the result.
 *
 * Camera frames arrive as CV_8UC4. cvtColor accepts four channel input for RGBA2GRAY, and the
 * edges are expanded back to RGBA because the preview surface expects four channels.
 */
JNIEXPORT void JNICALL
Java_com_os_cvcamera_ndk_NativeProcessor_cannyEdges(
        JNIEnv* /* env */, jobject /* thiz */, jlong matAddr, jdouble lowThreshold, jdouble highThreshold) {
    cv::Mat& rgba = *reinterpret_cast<cv::Mat*>(matAddr);

    cv::Mat gray;
    cv::cvtColor(rgba, gray, cv::COLOR_RGBA2GRAY);

    cv::Mat edges;
    cv::Canny(gray, edges, lowThreshold, highThreshold);

    cv::cvtColor(edges, rgba, cv::COLOR_GRAY2RGBA);
}

/**
 * Greyscale conversion in place.
 *
 * Converting straight back into the source Mat is not allowed, so the result is written to a
 * temporary and then assigned.
 */
JNIEXPORT void JNICALL
Java_com_os_cvcamera_ndk_NativeProcessor_greyscale(JNIEnv* /* env */, jobject /* thiz */, jlong matAddr) {
    cv::Mat& rgba = *reinterpret_cast<cv::Mat*>(matAddr);

    cv::Mat gray;
    cv::cvtColor(rgba, gray, cv::COLOR_RGBA2GRAY);
    cv::cvtColor(gray, rgba, cv::COLOR_GRAY2RGBA);
}

}  // extern "C"
