#include "org_meltzg_jmlm_device_MTPContentDevice.h"
#include "mtp_helpers.h"
#include "jni_helpers.h"

using namespace jmtp;

using std::vector;
using std::wstring;
using std::optional;

JNIEXPORT jobject JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_getDevicesInfo(JNIEnv *env, jclass obj)
{
	vector<MTPDeviceInfo> cDevices = getDevicesInfo();
	jobject jDevices = toJMTPDeviceInfoList(env, obj, cDevices);
	return jDevices;
}

JNIEXPORT jobject JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_getDeviceInfo(JNIEnv *env, jclass obj, jstring jId)
{
	wstring id = jStringToWString(env, jId);
	optional<MTPDeviceInfo> cDevice = getDeviceInfo(id);
	jobject jDevice = nullptr;
	if (cDevice)
	{
		jDevice = toJMTPDeviceInfo(env, obj, *cDevice);
	}
	return jDevice;
}

JNIEXPORT void JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_initMTP(JNIEnv *env, jclass obj)
{
	initMTP();
}

JNIEXPORT jobject JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_getChildIds(JNIEnv *env, jobject obj, jstring deviceId, jstring parentId)
{
	wstring cDeviceId = jStringToWString(env, deviceId);
	wstring cParentId = jStringToWString(env, parentId);
	vector<wstring> cChildIds = getChildIds(cDeviceId, cParentId);
	jobject jChildIds = getNewArrayList(env);
	return jChildIds;
}

JNIEXPORT jobject JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_createDirNode(JNIEnv *env, jobject obj, jstring deviceId, jstring parentId, jstring name)
{
	return nullptr;
}

JNIEXPORT jobject JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_createContentNode(JNIEnv *env, jobject obj, jstring deviceId, jstring parentId, jstring file)
{
	return nullptr;
}

JNIEXPORT jobject JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_readNode(JNIEnv *env, jobject obj, jstring deviceId, jstring parentId, jstring file)
{
	return nullptr;
}

JNIEXPORT jobject JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_copyNode(JNIEnv *env, jobject obj, jstring deviceId, jstring parentId, jstring id, jstring tmpFolder)
{
	return nullptr;
}

JNIEXPORT jboolean JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_deleteNode(JNIEnv *env, jobject obj, jstring deviceId, jstring id)
{
	return false;
}

JNIEXPORT jboolean JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_retrieveNode(JNIEnv *env, jobject obj, jstring deviceId, jstring id, jstring destFolder)
{
	return false;
}
