#include <iostream>
#include "org_meltzg_jmlm_device_MTPAudioContentDevice.h"
#include "mtp_helpers.h"
#include "jni_helpers.h"

using namespace jmtp;

using std::optional;
using std::vector;
using std::wstring;

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPAudioContentDevice_getStorageDevice(JNIEnv *env, jobject obj, jstring path, jstring deviceId)
{
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPAudioContentDevice_getDevicesInfo(JNIEnv *env, jobject cls)
{
  vector<MTPDeviceInfo> cdevices = getDevicesInfo();
  jobject jdevices = toJMTPDeviceInfoList(env, cls, cdevices);
  return jdevices;
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPAudioContentDevice_getDeviceInfo(JNIEnv *env, jobject cls, jstring deviceId)
{
  wstring id = jStringToWString(env, deviceId);
  optional<MTPDeviceInfo> cdevice = getDeviceInfo(id);
  jobject jDevice = nullptr;
  if (cdevice)
  {
    jDevice = toJMTPDeviceInfo(env, cls, *cdevice);
  }
  return jDevice;
}
