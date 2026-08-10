// Frame processing for the NDK example.
//
// Every effect below is the C++ counterpart of an effect the Kotlin example implements through the
// OpenCV Java bindings. Only the address of an OpenCV Mat crosses the JNI boundary, so no pixel
// data is copied.
//
// Camera frames arrive as CV_8UC4 (RGBA). Functions that need fewer channels convert into a
// temporary and write the result back, because OpenCV rejects an in place conversion.

#include <jni.h>
#include <string>

#include <android/log.h>
#include <opencv2/opencv.hpp>

#define LOG_TAG "CvCameraNdk"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

namespace {

/** Grey result expanded back to RGBA so the preview surface keeps four channels. */
void writeGreyAsRgba(const cv::Mat& grey, cv::Mat& rgba) {
    cv::cvtColor(grey, rgba, cv::COLOR_GRAY2RGBA);
}

cv::Mat& matOf(jlong addr) {
    return *reinterpret_cast<cv::Mat*>(addr);
}

}  // namespace

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_os_cvcamera_ndk_NativeEffects_openCvVersion(JNIEnv* env, jobject) {
    const std::string version = cv::getVersionString();
    LOGI("OpenCV native version: %s", version.c_str());
    return env->NewStringUTF(version.c_str());
}

JNIEXPORT void JNICALL
Java_com_os_cvcamera_ndk_NativeEffects_greyscale(JNIEnv*, jobject, jlong addr) {
    cv::Mat& rgba = matOf(addr);
    cv::Mat grey;
    cv::cvtColor(rgba, grey, cv::COLOR_RGBA2GRAY);
    writeGreyAsRgba(grey, rgba);
}

JNIEXPORT void JNICALL
Java_com_os_cvcamera_ndk_NativeEffects_canny(JNIEnv*, jobject, jlong addr, jdouble low, jdouble high) {
    cv::Mat& rgba = matOf(addr);
    cv::Mat grey, edges;
    cv::cvtColor(rgba, grey, cv::COLOR_RGBA2GRAY);
    cv::Canny(grey, edges, low, high);
    writeGreyAsRgba(edges, rgba);
}

JNIEXPORT void JNICALL
Java_com_os_cvcamera_ndk_NativeEffects_sobel(JNIEnv*, jobject, jlong addr) {
    cv::Mat& rgba = matOf(addr);
    cv::Mat grey, gradient, absolute;
    cv::cvtColor(rgba, grey, cv::COLOR_RGBA2GRAY);
    cv::Sobel(grey, gradient, CV_16S, 1, 1);
    cv::convertScaleAbs(gradient, absolute);
    writeGreyAsRgba(absolute, rgba);
}

JNIEXPORT void JNICALL
Java_com_os_cvcamera_ndk_NativeEffects_sepia(JNIEnv*, jobject, jlong addr) {
    cv::Mat& rgba = matOf(addr);
    // Row major sepia matrix for RGBA input, alpha left untouched.
    static const cv::Matx44f kSepia(
            0.393f, 0.769f, 0.189f, 0.0f,
            0.349f, 0.686f, 0.168f, 0.0f,
            0.272f, 0.534f, 0.131f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f);
    cv::transform(rgba, rgba, kSepia);
}

JNIEXPORT void JNICALL
Java_com_os_cvcamera_ndk_NativeEffects_blur(JNIEnv*, jobject, jlong addr, jint kernel) {
    cv::Mat& rgba = matOf(addr);
    // GaussianBlur needs an odd kernel size.
    const int size = (kernel % 2 == 0) ? kernel + 1 : kernel;
    cv::GaussianBlur(rgba, rgba, cv::Size(size, size), 0.0);
}

JNIEXPORT void JNICALL
Java_com_os_cvcamera_ndk_NativeEffects_negative(JNIEnv*, jobject, jlong addr) {
    cv::Mat& rgba = matOf(addr);
    cv::bitwise_not(rgba, rgba);
}

JNIEXPORT void JNICALL
Java_com_os_cvcamera_ndk_NativeEffects_sharpen(JNIEnv*, jobject, jlong addr) {
    cv::Mat& rgba = matOf(addr);
    static const cv::Matx33f kSharpen(
            0.0f, -1.0f, 0.0f,
            -1.0f, 5.0f, -1.0f,
            0.0f, -1.0f, 0.0f);
    cv::filter2D(rgba, rgba, -1, kSharpen);
}

JNIEXPORT void JNICALL
Java_com_os_cvcamera_ndk_NativeEffects_emboss(JNIEnv*, jobject, jlong addr) {
    cv::Mat& rgba = matOf(addr);
    static const cv::Matx33f kEmboss(
            -2.0f, -1.0f, 0.0f,
            -1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 2.0f);
    cv::Mat grey, embossed;
    cv::cvtColor(rgba, grey, cv::COLOR_RGBA2GRAY);
    cv::filter2D(grey, embossed, -1, kEmboss, cv::Point(-1, -1), 128.0);
    writeGreyAsRgba(embossed, rgba);
}

JNIEXPORT void JNICALL
Java_com_os_cvcamera_ndk_NativeEffects_binary(JNIEnv*, jobject, jlong addr, jdouble threshold) {
    cv::Mat& rgba = matOf(addr);
    cv::Mat grey, binary;
    cv::cvtColor(rgba, grey, cv::COLOR_RGBA2GRAY);
    cv::threshold(grey, binary, threshold, 255.0, cv::THRESH_BINARY);
    writeGreyAsRgba(binary, rgba);
}

JNIEXPORT void JNICALL
Java_com_os_cvcamera_ndk_NativeEffects_cartoon(JNIEnv*, jobject, jlong addr) {
    cv::Mat& rgba = matOf(addr);

    cv::Mat grey, edges;
    cv::cvtColor(rgba, grey, cv::COLOR_RGBA2GRAY);
    cv::medianBlur(grey, grey, 7);
    cv::adaptiveThreshold(grey, edges, 255.0, cv::ADAPTIVE_THRESH_MEAN_C, cv::THRESH_BINARY, 9, 9.0);

    // bilateralFilter only accepts CV_8UC1 or CV_8UC3, so drop alpha for the smoothing pass.
    cv::Mat rgb, smoothed, smoothedRgba, edgesRgba;
    cv::cvtColor(rgba, rgb, cv::COLOR_RGBA2RGB);
    cv::bilateralFilter(rgb, smoothed, 9, 300.0, 300.0);
    cv::cvtColor(smoothed, smoothedRgba, cv::COLOR_RGB2RGBA);
    cv::cvtColor(edges, edgesRgba, cv::COLOR_GRAY2RGBA);
    cv::bitwise_and(smoothedRgba, edgesRgba, rgba);
}

}  // extern "C"
