#ifndef CVCAMERA_MOBILE_CPP_LOGGER_H
#define CVCAMERA_MOBILE_CPP_LOGGER_H

#include <android/looper.h>
#include <android/native_window.h>
#include <camera/NdkCameraDevice.h>
#include <camera/NdkCameraManager.h>
#include <camera/NdkCameraMetadata.h>
#include <media/NdkImageReader.h>
#include <android/log.h>


class Logger {

    public:
        Logger();

        static void onSessionActive(void *context, ACameraCaptureSession *session);

        static void onSessionReady(void *context, ACameraCaptureSession *session);

        static void onSessionClosed(void *context, ACameraCaptureSession *session);

        static void
        onCaptureFailed(void *context, ACameraCaptureSession *session, ACaptureRequest *request,
                        ACameraCaptureFailure *failure);

        static void
        onCaptureSequenceCompleted(void *context, ACameraCaptureSession *session, int sequenceId,
                                   int64_t frameNumber);

        static void
        onCaptureSequenceAborted(void *context, ACameraCaptureSession *session, int sequenceId);

        static void
        onCaptureCompleted(void *context, ACameraCaptureSession *session, ACaptureRequest *request,
                           const ACameraMetadata *result);
    };


#endif //CVCAMERA_MOBILE_CPP_LOGGER_H
