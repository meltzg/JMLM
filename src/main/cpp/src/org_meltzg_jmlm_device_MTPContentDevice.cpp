#include "org_meltzg_jmlm_device_MTPContentDevice.h"
#include "mtpHelpers.h"
#include "jniHelpers.h"

using namespace LibJMTP;

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_getDevicesInfo
(JNIEnv *env, jclass obj) {
	vector<MTPDeviceInfo> info = getDevicesInfo();
	jobject jInfo = toJMTPDeviceInfoList(env, obj, info);
	return jInfo;
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_getDeviceInfo
(JNIEnv *env, jclass obj, jstring id) {
	wstring cId = jStringToWString(env, id);
	
	MTPDeviceInfo info = getDeviceInfo(cId);
	jobject jInfo = toJMTPDeviceInfo(env, obj, info);
	return jInfo;
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_getChildIds
(JNIEnv *env, jobject obj, jstring deviceId, jstring parentId) {
	wstring cDeviceId = jStringToWString(env, deviceId);
	wstring cParentId = jStringToWString(env, parentId);
	vector<wstring> childIds = getChildIds(cDeviceId, cParentId);

	jobject jList = getNewArrayList(env);
	for (auto iter = childIds.begin(); iter != childIds.end(); iter++) {
		jstring childId = wcharToJString(env, iter->c_str());
		arrayListAdd(env, jList, childId);
	}
	
	return jList;
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_createDirNode
(JNIEnv *env, jobject obj, jstring deviceId, jstring parentId, jstring name) {
	wstring cDeviceId = jStringToWString(env, deviceId);
	wstring cParentId = jStringToWString(env, parentId);
	wstring cName = jStringToWString(env, name);
	
	MTPContentNode node = createDirNode(cDeviceId, cParentId, cName);
	jobject jnode = nullptr;
	if (node.isValid) {
		jnode = toJMTPContentNode(env, node);
	}
	return jnode;
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_createContentNode
(JNIEnv *env, jobject obj, jstring deviceId, jstring parentId, jstring file) {
	wstring cDeviceId = jStringToWString(env, deviceId);
	wstring cParentId = jStringToWString(env, parentId);
	wstring cFile = jStringToWString(env, file);

	MTPContentNode node = createContentNode(cDeviceId, cParentId, cFile);
	jobject jnode = nullptr;
	if (node.isValid) {
		jnode = toJMTPContentNode(env, node);
	}
	return jnode;
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_readNode
(JNIEnv *env, jobject obj, jstring deviceId, jstring id) {
	wstring cDeviceId = jStringToWString(env, deviceId);
	wstring cId = jStringToWString(env, id);
	
	MTPContentNode node = readNode(cDeviceId, cId);
	jobject jnode = nullptr;
	if (node.isValid) {
		jnode = toJMTPContentNode(env, node);
	}
	return jnode;
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_copyNode
(JNIEnv *env, jobject obj, jstring deviceId, jstring parentId, jstring id, jstring tmpFolder) {
	wstring cDeviceId = jStringToWString(env, deviceId);
	wstring cParentId = jStringToWString(env, parentId);
	wstring cId = jStringToWString(env, id);
	wstring cTempFolder = jStringToWString(env, tmpFolder);

	MTPContentNode node = copyNode(cDeviceId, cParentId, cId, cTempFolder);
	jobject jnode = nullptr;
	if (node.isValid) {
		jnode = toJMTPContentNode(env, node);
	}
	return jnode;
}

JNIEXPORT jboolean JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_deleteNode
(JNIEnv *env, jobject obj, jstring deviceId, jstring id) {
	wstring cDeviceId = jStringToWString(env, deviceId);
	wstring cId = jStringToWString(env, id);
	
	return deleteNode(cDeviceId, cId);
}

JNIEXPORT jboolean JNICALL Java_org_meltzg_jmlm_device_MTPContentDevice_retrieveNode
(JNIEnv *env, jobject obj, jstring deviceId, jstring id, jstring destFolder) {
	wstring cDeviceId = jStringToWString(env, deviceId);
	wstring cId = jStringToWString(env, id);
	wstring cDestFolder = jStringToWString(env, destFolder);
	
	return retrieveNode(cDeviceId, cId, cDestFolder);
}