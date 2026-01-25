#ifndef CVCAMERA_MOBILE_CPP_NDKCAMERA_H
#define CVCAMERA_MOBILE_CPP_NDKCAMERA_H

#include <android/looper.h>
#include <android/native_window.h>
#include <android/sensor.h>
#include <camera/NdkCameraDevice.h>
#include <camera/NdkCameraManager.h>
#include <camera/NdkCameraMetadata.h>
#include <media/NdkImageReader.h>

class NdkCamera {

public:
    NdkCamera();

    virtual ~NdkCamera();

};


#endif //CVCAMERA_MOBILE_CPP_NDKCAMERA_H
