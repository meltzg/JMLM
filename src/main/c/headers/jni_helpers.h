#ifndef JNI_HELPERS_H
#define JNI_HELPERS_H

#include <jni.h>
#include "mtp_models.h"

static const char *const JCONSTRUCTOR = "<init>";
static const char *const JSTRING = "Ljava/lang/String;";
static const char *const JARRLIST = "Ljava/util/ArrayList;";
static const char *const JLIST = "Ljava/util/List;";

static const char *const JMTPDEVICEINFO = "Lorg/meltzg/jmlm/filesystem/mtp/MTPFileSystemProvider$MTPDeviceInfo;";
static const char *const JMTPCONTENTNODE = "Lorg/meltzg/jmlm/device/content/MTPContentNode;";
static const char *const JMTPSTORAGEDEVICE = "Lorg/meltzg/jmlm/device/storage/StorageDevice;";

jobject getNewArrayList(JNIEnv *env);
void arrayListAdd(JNIEnv *env, jobject list, jobject element);

jobject toJMTPDeviceInfo(JNIEnv *env, jobject obj, MTPDeviceInfo_t info);
jobject toJMTPDeviceInfoList(JNIEnv *env, jobject obj, MTPDeviceInfo_t *info, int numDevices);
jobject toJMTPStorageDevice(JNIEnv *env, MTPStorageDevice_t storage_device);

#endif
