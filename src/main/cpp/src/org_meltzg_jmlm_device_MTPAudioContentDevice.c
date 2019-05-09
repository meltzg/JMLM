#include "org_meltzg_jmlm_device_MTPAudioContentDevice.h"
#include "jni_helpers.h"

/*
 * Class:     org_meltzg_jmlm_device_MTPAudioContentDevice
 * Method:    getStorageDevice
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Lorg/meltzg/jmlm/device/storage/StorageDevice;
 */
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPAudioContentDevice_getStorageDevice(JNIEnv *env, jobject obj, jstring path, jstring deviceId)
{
  return NULL;
}

/*
 * Class:     org_meltzg_jmlm_device_MTPAudioContentDevice
 * Method:    getDevicesInfo
 * Signature: ()Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPAudioContentDevice_getDevicesInfo(JNIEnv *env, jclass cls)
{
  jobject newList = getNewArrayList(env);
  return newList;
}

/*
 * Class:     org_meltzg_jmlm_device_MTPAudioContentDevice
 * Method:    getDeviceInfo
 * Signature: (Ljava/lang/String;)Lorg/meltzg/jmlm/device/MTPAudioContentDevice/MTPDeviceInfo;
 */
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPAudioContentDevice_getDeviceInfo(JNIEnv *env, jclass cls, jstring deviceId)
{
  return NULL;
}
