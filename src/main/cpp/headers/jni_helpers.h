#pragma once
#include <jni.h>
#include "mtp_models.h"

const char *const JCONSTRUCTOR = "<init>";
const char *const JSTRING = "Ljava/lang/String;";
const char *const JARRLIST = "Ljava/util/ArrayList;";
const char *const JLIST = "Ljava/util/List;";

const char *const JMTPDEVICEINFO = "Lorg/meltzg/jmlm/device/MTPAudioContentDevice$MTPDeviceInfo;";
const char *const JMTPCONTENTNODE = "Lorg/meltzg/jmlm/device/content/MTPContentNode;";
const char *const JMTPSTORAGEDEVICE = "Lorg/meltzg/jmlm/device/storage/StorageDevice;";

jstring cstrToJString(JNIEnv *env, const char *str);
jobject getNewArrayList(JNIEnv *env);
void arrayListAdd(JNIEnv *env, jobject list, jobject element);

jobject toJMTPDeviceInfo(JNIEnv *env, jobject obj, MTPDeviceInfo info);
jobject toJMTPDeviceInfoList(JNIEnv *env, jobject obj, MTPDeviceInfo *info);
jobject toJMTPStorageDevice(JNIEnv *env, MTPStorageDevice storage_device);
