#include <stdlib.h>
#include <stdio.h>
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
    MTPDeviceInfo *deviceList = NULL;
    unsigned int numDevices = getDevicesInfo(&deviceList);;

    jobject newList = toJMTPDeviceInfoList(env, cls, deviceList, numDevices);
    if (deviceList != NULL && numDevices > 0)
    {
        for (int i = 0; i < numDevices; i++)
        {
            freeMTPDeviceInfo(deviceList[i]);
        }
        free(deviceList);
    }
    return newList;
}

/*
* Class:     org_meltzg_jmlm_device_MTPAudioContentDevice
* Method:    getDeviceInfo
* Signature: (Ljava/lang/String;)Lorg/meltzg/jmlm/device/MTPAudioContentDevice/MTPDeviceInfo;
*/
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPAudioContentDevice_getDeviceInfo(JNIEnv *env, jclass cls, jstring deviceId)
{
    const char *cDeviceId = (*env)->GetStringUTFChars(env, deviceId, NULL);
    return NULL;
}
