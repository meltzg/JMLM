#include <stdlib.h>
#include <stdio.h>
#include "org_meltzg_jmlm_device_MTPAudioContentDevice.h"
#include "jni_helpers.h"
#include "mtp_helpers.h"

/*
* Class:     org_meltzg_jmlm_device_MTPAudioContentDevice
* Method:    getStorageDevice
* Signature: (Ljava/lang/String;Ljava/lang/String;)Lorg/meltzg/jmlm/device/storage/StorageDevice;
*/
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPAudioContentDevice_getStorageDevice(JNIEnv *env, jobject obj, jstring path, jstring deviceId)
{
    const char *cDeviceId = (*env)->GetStringUTFChars(env, deviceId, NULL);
    const char *cPath = (*env)->GetStringUTFChars(env, path, NULL);
    MTPStorageDevice storageDevice;

    if (getStorageDevice(&storageDevice, cDeviceId, cPath))
    {
        printf("storage id: %s, capacity: %llu, freespace: %llu\n", storageDevice.storage_id, storageDevice.capacity, storageDevice.free_space);
        jobject jstorage = toJMTPStorageDevice(env, storageDevice);
        freeMTPStorageDevice(storageDevice);
        return jstorage;
    }

    return NULL;
}
