//
// Created by Admin on 2021/1/28.
//

#ifndef PLAY_CALL_BACK_FROM_JNI_UTIL_H
#define PLAY_CALL_BACK_FROM_JNI_UTIL_H

#include <jni.h>
#include "android_log.h"

#define THREAD_MAIN 0
#define THREAD_CHILD 1

class CallbackFromJniUtil {

public:
    CallbackFromJniUtil(JavaVM *javaVm_, JNIEnv *jniEnv_);

    void x264EncodeDataCallback(
                                char *javaMethodName,
                                char *methodSig,
                                char *data,
                                int dataLength);

    void setClazz(jobject &clazz_);

private:
    JavaVM *javaVm;
    JNIEnv *jniEnv;
    jobject clazz;
};


#endif //PLAY_CALL_BACK_FROM_JNI_UTIL_H
