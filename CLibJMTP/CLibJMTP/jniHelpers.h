#pragma once

#include <jni.h>
#include "mtpHelpers.h"

#define JSTRING "Ljava/lang/String;"
#define JARRLIST "Ljava/util/ArrayList;"
#define JMTPDEV "Lorg/meltzg/jmtp/models/MTPDevice;"

jstring wcharToJString(JNIEnv *env, const wchar_t* wstr);
wchar_t * jStringToWchar(JNIEnv *env, jstring jStr);
jobject mtpdToJMptd(JNIEnv *env, MTPDevice mtpd);
jobject getNewArrayList(JNIEnv *env);
void arrayListAdd(JNIEnv *env, jobject list, jobject element);
jobject getNewMTPDevice(JNIEnv *env, const wchar_t *devId, const wchar_t *devFName, const wchar_t *devDesc, const wchar_t *devManu);