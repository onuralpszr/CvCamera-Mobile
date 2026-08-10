# Native layer for the NDK example. All frame processing happens here in C++; the Kotlin side
# only owns the camera preview and hands over the Mat address.
cmake_minimum_required(VERSION 3.22.1)

project("cvcamerandk")

# OpenCV_DIR is supplied by build.gradle.kts and points at the prepared Android SDK.
find_package(OpenCV REQUIRED COMPONENTS opencv_java)

add_library(cvcamerandk SHARED native-lib.cpp)

find_library(log-lib log)

target_link_libraries(cvcamerandk ${OpenCV_LIBS} ${log-lib})

# Android 15 and newer can use 16 KB memory pages.
target_link_options(cvcamerandk PRIVATE "-Wl,-z,max-page-size=16384")
