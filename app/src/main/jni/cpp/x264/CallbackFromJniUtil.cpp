//
// Created by Admin on 2021/1/28.
//
#include "call_back_from_jni_util.h"

CallbackFromJniUtil::CallbackFromJniUtil(JavaVM *javaVm_, JNIEnv *jniEnv_) {
    javaVm = javaVm_;
    jniEnv = jniEnv_;
}

void CallbackFromJniUtil::setClazz(jobject &clazz_) {

    clazz = jniEnv->NewGlobalRef(clazz_);

}

void CallbackFromJniUtil::x264EncodeDataCallback(char *javaMethodName,
                                                 char *methodSig,
                                                 char *data,
                                                 int dataLength) {

    int attachedHere = 0; // know if detaching at the end is necessary
    JNIEnv *env_;
    jint ret = javaVm->GetEnv((void **) &env_, JNI_VERSION_1_6);

    if (JNI_EDETACHED == ret) {
        // Supported but not attached yet, needs to call AttachCurrentThread
        ret = javaVm->AttachCurrentThread(&env_, 0);
        if (JNI_OK == ret) {
            attachedHere = 1;
        } else {
            // Failed to attach, cancel
            return;
        }
    } else if (JNI_OK == ret) {
        // Current thread already attached, do not attach 'again' (just to save the attachedHere flag)
        // We make sure to keep attachedHere = 0
    } else {
        // JNI_EVERSION, specified version is not supported cancel this..
        return;
    }

    // Execute code using NewEnv
    jclass clazz1 = env_->GetObjectClass(clazz);
    jmethodID methodId = env_->GetMethodID(clazz1, javaMethodName, methodSig);
    jbyteArray array = env_->NewByteArray(dataLength);
    env_->SetByteArrayRegion(array, 0, dataLength, (jbyte *) data);
    env_->CallVoidMethod(clazz, methodId, array);

    if (attachedHere) { // Key check
        javaVm->DetachCurrentThread(); // Done only when attachment was done here
    }

}
