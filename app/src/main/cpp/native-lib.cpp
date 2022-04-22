#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>


extern "C" JNIEXPORT jstring JNICALL
Java_com_al_cvcamera_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    auto a = CV_VERSION_MAJOR;
    std::string hello = "Hello from C++";
    return env->NewStringUTF(std::to_string(a).c_str());
}