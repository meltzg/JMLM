#include "org_meltzg_jmlm_device_MTPContentDevice.h"
#include "mtp_helpers.h"
#include "jni_helpers.h"

using namespace jmtp;

using std::optional;
using std::vector;
using std::wstring;

JNIEXPORT jobject JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_getDevicesInfo(JNIEnv *env, jclass obj)
{
	vector<MTPDeviceInfo> cdevices = getDevicesInfo();
	jobject jdevices = toJMTPDeviceInfoList(env, obj, cdevices);
	return jdevices;
}

JNIEXPORT jobject JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_getDeviceInfo(JNIEnv *env, jclass obj, jstring jId)
{
	wstring id = jStringToWString(env, jId);
	optional<MTPDeviceInfo> cdevice = getDeviceInfo(id);
	jobject jDevice = nullptr;
	if (cdevice)
	{
		jDevice = toJMTPDeviceInfo(env, obj, *cdevice);
	}
	return jDevice;
}

JNIEXPORT void JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_initMTP(JNIEnv *env, jclass obj)
{
	initMTP();
}

JNIEXPORT jobject JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_getChildIds(JNIEnv *env, jobject obj, jstring device_id, jstring parent_id)
{
	wstring cdevice_id = jStringToWString(env, device_id);
	wstring cparent_id = jStringToWString(env, parent_id);
	vector<wstring> cChildIds = getChildIds(cdevice_id, cparent_id);
	jobject jChildIds = getNewArrayList(env);
	return jChildIds;
}

JNIEXPORT jobject JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_createDirNode(JNIEnv *env, jobject obj, jstring device_id, jstring parent_id, jstring name)
{
	return nullptr;
}

JNIEXPORT jobject JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_createContentNode(JNIEnv *env, jobject obj, jstring device_id, jstring parent_id, jstring file)
{
	return nullptr;
}

JNIEXPORT jobject JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_readNode(JNIEnv *env, jobject obj, jstring device_id, jstring parent_id, jstring file)
{
	return nullptr;
}

JNIEXPORT jobject JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_copyNode(JNIEnv *env, jobject obj, jstring device_id, jstring parent_id, jstring id, jstring tmp_folder)
{
	return nullptr;
}

JNIEXPORT jboolean JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_deleteNode(JNIEnv *env, jobject obj, jstring device_id, jstring id)
{
	return false;
}

JNIEXPORT jboolean JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_retrieveNode(JNIEnv *env, jobject obj, jstring device_id, jstring id, jstring dest_folder)
{
	return false;
}

JNIEXPORT jobject JNICALL
Java_org_meltzg_jmlm_device_MTPContentDevice_getStorageDevice(JNIEnv *env, jobject obj, jstring device_id, jstring id) {
	return nullptr;
}
