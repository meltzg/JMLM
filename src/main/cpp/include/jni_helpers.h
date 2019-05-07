#pragma once
#include <jni.h>
#include <vector>
#include "mtp_models.h"

namespace jmtp
{
const char *const JCONSTRUCTOR = "<init>";
const char *const JSTRING = "Ljava/lang/String;";
const char *const JARRLIST = "Ljava/util/ArrayList;";
const char *const JLIST = "Ljava/util/List;";

const char *const JMTPDEVICEINFO = "Lorg/meltzg/jmlm/device/MTPAudioContentDevice$MTPDeviceInfo;";
const char *const JMTPCONTENTNODE = "Lorg/meltzg/jmlm/device/content/MTPContentNode;";
const char *const JMTPSTORAGEDEVICE = "Lorg/meltzg/jmlm/device/storage/StorageDevice;";

jstring wcharToJString(JNIEnv *env, const wchar_t *wstr);
std::wstring jStringToWString(JNIEnv *env, jstring jStr);
jobject getNewArrayList(JNIEnv *env);
void arrayListAdd(JNIEnv *env, jobject list, jobject element);

jobject toJMTPDeviceInfo(JNIEnv *env, jobject obj, MTPDeviceInfo info);
jobject toJMTPDeviceInfoList(JNIEnv *env, jobject obj, std::vector<MTPDeviceInfo> info);
jobject toJMTPStorageDevice(JNIEnv *env, MTPStorageDevice storage_device);
} // namespace jmtp