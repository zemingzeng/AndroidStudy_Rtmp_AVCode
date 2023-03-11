//
// Created by Admin on 2021/1/27.
//

#ifndef PLAY_ANDROID_LOG_H
#define PLAY_ANDROID_LOG_H

#include <android/log.h>

#define log(...) __android_log_print(ANDROID_LOG_INFO,"22m jni",__VA_ARGS__)
#endif //PLAY_ANDROID_LOG_H
