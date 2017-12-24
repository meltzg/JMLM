/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_meltzg_jmlm_device_MTPContentDevice */

#ifndef _Included_org_meltzg_jmlm_device_MTPContentDevice
#define _Included_org_meltzg_jmlm_device_MTPContentDevice
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_meltzg_jmlm_device_MTPContentDevice
 * Method:    getDevicesInfo
 * Signature: ()Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_getDevicesInfo
  (JNIEnv *, jclass);

/*
 * Class:     org_meltzg_jmlm_device_MTPContentDevice
 * Method:    getDeviceInfo
 * Signature: (Ljava/lang/String;)Lorg/meltzg/jmlm/device/MTPContentDevice/MTPDeviceInfo;
 */
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_getDeviceInfo
  (JNIEnv *, jclass, jstring);

/*
 * Class:     org_meltzg_jmlm_device_MTPContentDevice
 * Method:    getChildIds
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_getChildIds
  (JNIEnv *, jobject, jstring, jstring);

/*
 * Class:     org_meltzg_jmlm_device_MTPContentDevice
 * Method:    createDirNode
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lorg/meltzg/jmlm/device/content/AbstractContentNode;
 */
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_createDirNode
  (JNIEnv *, jobject, jstring, jstring, jstring);

/*
 * Class:     org_meltzg_jmlm_device_MTPContentDevice
 * Method:    createContentNode
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lorg/meltzg/jmlm/device/content/AbstractContentNode;
 */
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_createContentNode
  (JNIEnv *, jobject, jstring, jstring, jstring);

/*
 * Class:     org_meltzg_jmlm_device_MTPContentDevice
 * Method:    readNode
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Lorg/meltzg/jmlm/device/content/AbstractContentNode;
 */
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_readNode
  (JNIEnv *, jobject, jstring, jstring);

/*
 * Class:     org_meltzg_jmlm_device_MTPContentDevice
 * Method:    copyNode
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lorg/meltzg/jmlm/device/content/AbstractContentNode;
 */
JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_copyNode
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring);

/*
 * Class:     org_meltzg_jmlm_device_MTPContentDevice
 * Method:    deleteNode
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_deleteNode
  (JNIEnv *, jobject, jstring, jstring);

/*
 * Class:     org_meltzg_jmlm_device_MTPContentDevice
 * Method:    retrieveNode
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_retrieveNode
  (JNIEnv *, jobject, jstring, jstring, jstring);

#ifdef __cplusplus
}
#endif
#endif
