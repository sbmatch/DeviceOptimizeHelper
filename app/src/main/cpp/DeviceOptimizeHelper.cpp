// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("DeviceOptimizeHelper");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("DeviceOptimizeHelper")
//      }
//    }
#include <jni.h>
#include <sstream>
#include <android/log.h>
#include <android/binder_ibinder.h>
#include <android/binder_ibinder_jni.h>
#include <android/binder_interface_utils.h>
#include <dlfcn.h>
#include <stdexcept>
#include <sys/socket.h>
#include <syscall.h>
#include "include/JNIEnvPtr.h"
#define mLOG_TAG "DeviceOpt-jni"
#define LOGI(...) ((void) __android_log_print(ANDROID_LOG_INFO, mLOG_TAG, __VA_ARGS__))
#define LOGD(...) ((void) __android_log_print(ANDROID_LOG_DEBUG, mLOG_TAG, __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, mLOG_TAG, __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, mLOG_TAG, __VA_ARGS__))


static JavaVM *jvm;

// Caching the JVM on JVM OnLoad
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    jvm = vm;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_sbmatch_deviceopt_Utils_nativeInvoke_OptimizeHelperNative_optimize(JNIEnv *env,
                                                                            jobject thiz) {
}
