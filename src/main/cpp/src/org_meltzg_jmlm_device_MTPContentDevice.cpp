#include "org_meltzg_jmlm_device_MTPContentDevice.h"
#include "mtpHelpers.h"
#include "jniHelpers.h"

using LibJMTP::getDevicesInfo;
using LibJMTP::getDeviceInfo;
using LibJMTP::MTPDeviceInfo;
using LibJMTP::toJMTPDeviceInfoList;
using LibJMTP::toJMTPDeviceInfo;
using LibJMTP::jStringToWchar;

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_getDevicesInfo
(JNIEnv *env, jclass obj) {
	vector<MTPDeviceInfo> info = getDevicesInfo();
	jobject jInfo = toJMTPDeviceInfoList(env, obj, info);
	return jInfo;
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_getDeviceInfo
(JNIEnv *env, jclass obj, jstring id) {
	wstring cId(jStringToWchar(env, id));
	MTPDeviceInfo info = getDeviceInfo(cId);
	jobject jInfo = toJMTPDeviceInfo(env, obj, info);
	return jInfo;
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_getChildIds
(JNIEnv *env, jobject obj, jstring deviceId, jstring parentId) {
	return NULL;
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_createDirNode
(JNIEnv *env, jobject obj, jstring deviceId, jstring parentId, jstring name) {
	return NULL;
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_createContentNode
(JNIEnv *env, jobject obj, jstring deviceId, jstring parentId, jstring file) {
	return NULL;
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_readNode
(JNIEnv *env, jobject obj, jstring deviceId, jstring id) {
	return NULL;
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_copyNode
(JNIEnv *env, jobject obj, jstring deviceId, jstring parentId, jstring id, jstring tmpFolder) {
	return NULL;
}

JNIEXPORT jboolean JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_deleteNode
(JNIEnv *env, jobject obj, jstring deviceId, jstring id) {
	return false;
}

JNIEXPORT jboolean JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_retrieveNode
(JNIEnv *env, jobject obj, jstring deviceId, jstring id, jstring destFolder) {
	return false;
}