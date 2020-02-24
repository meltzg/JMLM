#include <stdlib.h>
#include <stdint.h>
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
    jobject jdevice = NULL;
    if (getDeviceInfo(&deviceInfo, cDeviceId))
    {
        jdevice = toJMTPDeviceInfo(env, cls, deviceInfo);
        freeMTPDeviceInfo(deviceInfo);
    }
    (*env)->ReleaseStringUTFChars(env, deviceId, cDeviceId);
    return jdevice;
}

/*
 * Class:     org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider
 * Method:    getFileStoreId
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider_getFileStoreId(JNIEnv *env, jobject obj, jstring path, jstring deviceId)
{
    const char *cDeviceId = (*env)->GetStringUTFChars(env, deviceId, NULL);
    const char *cPath = (*env)->GetStringUTFChars(env, path, NULL);
    char *storageId = NULL;
    jobject jstorage = NULL;

    if (storageId = getStorageDeviceId(cDeviceId, cPath))
    {
        printf("storage id: %s\n", storageId);
        jstorage = (*env)->NewStringUTF(env, storageId);
        free(storageId);
    }
    (*env)->ReleaseStringUTFChars(env, deviceId, cDeviceId);
    (*env)->ReleaseStringUTFChars(env, path, cPath);

    return jstorage;
}

/*
 * Class:     org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider
 * Method:    getFileContent
 * Signature: (Ljava/lang/String;Ljava/lang/String;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider_getFileContent(JNIEnv *env, jobject obj, jstring path, jstring deviceId)
{
    const char *cDeviceId = (*env)->GetStringUTFChars(env, deviceId, NULL);
    const char *cPath = (*env)->GetStringUTFChars(env, path, NULL);
    jbyteArray content = NULL;

    uint64_t size;
    uint8_t *fileContent = getFileContent(cDeviceId, cPath, &size);
    if (fileContent != NULL)
    {
        content = (*env)->NewByteArray(env, size);
        (*env)->SetByteArrayRegion(env, content, 0, size, fileContent);
        free(fileContent);
    }

    (*env)->ReleaseStringUTFChars(env, deviceId, cDeviceId);
    (*env)->ReleaseStringUTFChars(env, path, cPath);
    return content;
}

/*
 * Class:     org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider
 * Method:    getPathChildren
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider_getPathChildren(JNIEnv *env, jobject obj, jstring path, jstring deviceId)
{
    const char *cDeviceId = (*env)->GetStringUTFChars(env, deviceId, NULL);
    const char *cPath = (*env)->GetStringUTFChars(env, path, NULL);
    (*env)->ReleaseStringUTFChars(env, deviceId, cDeviceId);
    (*env)->ReleaseStringUTFChars(env, path, cPath);
    return NULL;
}

/*
 * Class:     org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider
 * Method:    isDirectory
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider_isDirectory(JNIEnv *env, jobject obj, jstring path, jstring deviceId)
{
    const char *cDeviceId = (*env)->GetStringUTFChars(env, deviceId, NULL);
    const char *cPath = (*env)->GetStringUTFChars(env, path, NULL);
    bool isDir = isDirectory(cDeviceId, cPath);
    (*env)->ReleaseStringUTFChars(env, deviceId, cDeviceId);
    (*env)->ReleaseStringUTFChars(env, path, cPath);
    return isDir;
}

/*
 * Class:     org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider
 * Method:    size
 * Signature: (Ljava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider_size(JNIEnv *env, jobject obj, jstring path, jstring deviceId)
{
    const char *cDeviceId = (*env)->GetStringUTFChars(env, deviceId, NULL);
    const char *cPath = (*env)->GetStringUTFChars(env, path, NULL);
    (*env)->ReleaseStringUTFChars(env, deviceId, cDeviceId);
    (*env)->ReleaseStringUTFChars(env, path, cPath);
    return 0;
}

/*
 * Class:     org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider
 * Method:    getFileStoreProperties
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Lorg/meltzg/jmlm/device/storage/StorageDevice;
 */
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_filesystem_mtp_MTPFileSystemProvider_getFileStoreProperties(JNIEnv *env, jobject obj, jstring storageId, jstring deviceId)
{
    const char *cDeviceId = (*env)->GetStringUTFChars(env, deviceId, NULL);
    const char *cStorageId = (*env)->GetStringUTFChars(env, storageId, NULL);
    MTPStorageDevice storageDevice;
    jobject jstorage = NULL;

    if (getStorageDevice(&storageDevice, cDeviceId, cStorageId))
    {
        jstorage = toJMTPStorageDevice(env, storageDevice);
        freeMTPStorageDevice(storageDevice);
    }
    (*env)->ReleaseStringUTFChars(env, deviceId, cDeviceId);
    (*env)->ReleaseStringUTFChars(env, storageId, cStorageId);

    return jstorage;
}
