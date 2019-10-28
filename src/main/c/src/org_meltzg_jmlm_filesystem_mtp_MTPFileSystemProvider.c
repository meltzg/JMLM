#include "org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider.h"
#include "jni_helpers.h"
#include "mtp_helpers.h"

/*
 * Class:     org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider
 * Method:    initMTP
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider_initMTP(JNIEnv *env, jclass cls)
{
    initMTP();
}

/*
* Class:     org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider
* Method:    getDevicesInfo
* Signature: ()Ljava/util/List;
*/
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider_getDevicesInfo(JNIEnv *env, jclass cls)
{
    MTPDeviceInfo *deviceList = NULL;
    int numDevices = getDevicesInfo(&deviceList);

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
* Class:     org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider
* Method:    getDeviceInfo
 * Signature: (Ljava/lang/String;)Lorg/meltzg/jmlm/filesystem/mtp/MTPFileSystemProvider/MTPDeviceInfo;
*/
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider_getDeviceInfo(JNIEnv *env, jclass cls, jstring deviceId)
{
    const char *cDeviceId = (*env)->GetStringUTFChars(env, deviceId, NULL);
    MTPDeviceInfo deviceInfo;
    if (getDeviceInfo(&deviceInfo, cDeviceId))
    {
        jobject jdevice = toJMTPDeviceInfo(env, cls, deviceInfo);
        freeMTPDeviceInfo(deviceInfo);
        return jdevice;
    }
    return NULL;
}

/*
* Class:     org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider
 * Method:    getFileStore
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Lorg/meltzg/jmlm/filesystem/mtp/MTPFileStore;
 */
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider_getFileStore(JNIEnv *env, jobject obj, jstring path, jstring deviceId)
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
