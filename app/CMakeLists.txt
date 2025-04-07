
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.31.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("optimizeHelper")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.

find_library(libandroid android)
find_library(libbinder_ndk binder_ndk)
find_library(liblog log)
find_library(libnativewindow nativewindow)
find_library(libdl dl)

find_package(Threads REQUIRED) #线程支持

#set(LIBRARY_PATH ${CMAKE_SOURCE_DIR}/ndk/lib/${ANDROID_ABI})

#add_library(utils SHARED IMPORTED)
#set_target_properties(utils PROPERTIES IMPORTED_LOCATION ${LIBRARY_PATH}/libutils.so)

add_library(${CMAKE_PROJECT_NAME} SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        ${CMAKE_SOURCE_DIR}/src/main/cpp/DeviceOptimizeHelper.cpp)

# 设置输出目录
set_target_properties(${CMAKE_PROJECT_NAME} PROPERTIES
        LIBRARY_OUTPUT_DIRECTORY
        ${PROJECT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}
        )

target_include_directories(${CMAKE_PROJECT_NAME} PUBLIC ${CMAKE_SOURCE_DIR}/src/main/cpp/include)

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        ${libandroid}
        ${libbinder_ndk}
        ${liblog}
        ${libdl}
        c++_shared
        )
target_link_options(${CMAKE_PROJECT_NAME} PRIVATE "-Wl,-z,max-page-size=16384")




