//
// Created by sbmatch on 2025/3/2.
//

#ifndef DEVICEOPTIMIZEHELPER_JNIENVPTR_H
#define DEVICEOPTIMIZEHELPER_JNIENVPTR_H

#include <jni.h>

extern JavaVM* GetJVM();

class JNIEnvPtr {
public:
    JNIEnvPtr() : env_{nullptr}, need_detach_{false} {
        if (GetJVM()->GetEnv((void**) &env_, JNI_VERSION_1_6) ==
            JNI_EDETACHED) {
            GetJVM()->AttachCurrentThread(&env_, nullptr);
            need_detach_ = true;
        }
    }

    ~JNIEnvPtr() {
        if (need_detach_) {
            GetJVM()->DetachCurrentThread();
        }
    }

    JNIEnv* operator->() {
        return env_;
    }

private:
    JNIEnvPtr(const JNIEnvPtr&) = delete;
    JNIEnvPtr& operator=(const JNIEnvPtr&) = delete;

private:
    JNIEnv* env_;
    bool need_detach_;
};


#endif //DEVICEOPTIMIZEHELPER_JNIENVPTR_H
