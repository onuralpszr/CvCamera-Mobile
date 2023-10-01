//
// Created by Onuralp SEZER on 1.10.2023.
//

#ifndef CVCAMERA_NDKLOGGER_H
#define CVCAMERA_NDKLOGGER_H

#include <android/log.h>

#define LOG_TAG "CV_CAMERA"

#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,    LOG_TAG, __VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,       LOG_TAG, __VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,      LOG_TAG, __VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,       LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,      LOG_TAG, __VA_ARGS__)

#endif //CVCAMERA_NDKLOGGER_H
