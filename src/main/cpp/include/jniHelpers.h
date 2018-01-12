#pragma once
#include <jni.h>
#include <vector>
#include "mtpModels.h"

namespace LibJMTP {
    const char * const JCONSTRUCTOR = "<init>";
    const char * const JSTRING = "Ljava/lang/String;";
    const char * const JARRLIST = "Ljava/util/ArrayList;";
    const char * const JLIST = "Ljava/util/List;";
    const char * const JBIGINT = "Ljava/math/BigInteger;";

	const char * const JMTPDEVICE = "Lorg/meltzg/jmlm/device/MTPContentDevice;";
	const char * const JMTPDEVICEINFO = "Lorg/meltzg/jmlm/device/MTPContentDevice$MTPDeviceInfo;";
	const char * const JMTPCONTENTNODE = "Lorg/meltzg/jmlm/device/content/MTPContentNode;";

    jstring wcharToJString(JNIEnv *env, const wchar_t* wstr);
    jobject ulonglongToJBigInt(JNIEnv *env, unsigned long long num);
    wstring jStringToWString(JNIEnv *env, jstring jStr);
    jobject getNewArrayList(JNIEnv *env);
    void arrayListAdd(JNIEnv *env, jobject list, jobject element);

	jobject toJMTPDeviceInfo(JNIEnv *env, jobject obj, MTPDeviceInfo info);
	jobject toJMTPDeviceInfoList(JNIEnv *env, jobject obj, std::vector<MTPDeviceInfo> info);
	jobject toJMTPContentNode(JNIEnv *env, MTPContentNode node);
}