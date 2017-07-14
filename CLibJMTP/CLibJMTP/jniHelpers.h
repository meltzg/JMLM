#pragma once

#include <jni.h>
#include "mtpHelpers.h"

#define JSTRING "Ljava/lang/String;"
#define JARRLIST "Ljava/util/ArrayList;"
#define JMTPDEV "Lorg/meltzg/jmtp/mtp/MTPDevice;"

jstring wcharToJString(JNIEnv *env, wchar_t* wstr);
wchar_t * jStringToWchar(JNIEnv *env, jstring jStr);
jobject mtpdToJMptd(JNIEnv *env, MTPDevice mtpd);
jobject getNewArrayList(JNIEnv *env);
void arrayListAdd(JNIEnv *env, jobject list, jobject element);
jobject getNewMTPDevice(JNIEnv *env, wchar_t *devId, wchar_t *devFName, wchar_t *devDesc, wchar_t *devManu);