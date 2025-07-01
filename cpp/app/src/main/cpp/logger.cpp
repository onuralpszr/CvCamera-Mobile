#include "logger.h"


Logger::Logger() {}

Logger::~Logger() {}

void Logger::onSessionActive(void *context, ACameraCaptureSession *session) {
    __android_log_print(ANDROID_LOG_WARN, "NdkCamera", "onSessionActive %p", session);
}

void Logger::onSessionReady(void *context, ACameraCaptureSession *session) {
    __android_log_print(ANDROID_LOG_WARN, "NdkCamera", "onSessionReady %p", session);
}

void Logger::onSessionClosed(void *context, ACameraCaptureSession *session) {
    __android_log_print(ANDROID_LOG_WARN, "NdkCamera", "onSessionClosed %p", session);
}

void
Logger::onCaptureFailed(void *context, ACameraCaptureSession *session, ACaptureRequest *request,
                        ACameraCaptureFailure *failure) {
    __android_log_print(ANDROID_LOG_WARN, "NdkCamera", "onCaptureFailed %p %p %p", session, request,
                        failure);
}

void
Logger::onCaptureSequenceCompleted(void *context, ACameraCaptureSession *session, int sequenceId,
                                   int64_t frameNumber) {
    __android_log_print(ANDROID_LOG_WARN, "NdkCamera", "onCaptureSequenceCompleted %p %d %ld",
                        session, sequenceId, frameNumber);
}

void
Logger::onCaptureSequenceAborted(void *context, ACameraCaptureSession *session, int sequenceId) {
    __android_log_print(ANDROID_LOG_WARN, "NdkCamera", "onCaptureSequenceAborted %p %d", session,
                        sequenceId);
}

void
Logger::onCaptureCompleted(void *context, ACameraCaptureSession *session, ACaptureRequest *request,
                           const ACameraMetadata *result) {
    __android_log_print(ANDROID_LOG_WARN, "NdkCamera", "onCaptureCompleted %p %p %p", session,
                        request, result);
}
