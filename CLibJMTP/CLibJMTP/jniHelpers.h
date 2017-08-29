#pragma once

#include <jni.h>
#include "mtpHelpers.h"

#define JSTRING "Ljava/lang/String;"
#define JARRLIST "Ljava/util/ArrayList;"
#define JLIST "Ljava/util/List;"
#define JBIGINT "Ljava/math/BigInteger;"
#define JMTPDEV "Lorg/meltzg/jmlm/device/models/MTPContentDevice;"
#define JMTPOTREE "Lorg/meltzg/jmlm/content/models/MTPContentTree;"

#define JCONSTRUCTOR "<init>"

jstring wcharToJString(JNIEnv *env, const wchar_t* wstr);
jobject ulonglongToJBigInt(JNIEnv *env, unsigned long long num);
wchar_t * jStringToWchar(JNIEnv *env, jstring jStr);
jobject mtpdToJMtpd(JNIEnv *env, MTPDevice mtpd);
jobject mtpotToJMtpot(JNIEnv *env, MTPObjectTree *mtpot);
jobject getNewArrayList(JNIEnv *env);
void arrayListAdd(JNIEnv *env, jobject list, jobject element);
jobject getNewMTPDevice(JNIEnv *env, const wchar_t *devId, const wchar_t *devFName, const wchar_t *devDesc, const wchar_t *devManu);