#include <jni.h>
#include <iostream>

#include "../../jMTP/bin/org_meltzg_jmtp_JMTP.h"
#include "jniHelpers.h"
#include "mtpHelpers.h"

using std::cerr;
using std::endl;
using std::nothrow;

using Microsoft::WRL::ComPtr;

JNIEXPORT jobject JNICALL Java_org_meltzg_jmtp_JMTP_getDevices
(JNIEnv *env, jobject obj) {
	vector<MTPDevice> devList = getDevices();
	jobject jDevList = getNewArrayList(env);

	for (int i = 0; i < devList.size(); i++) {
		arrayListAdd(env, jDevList, mtpdToJMtpd(env, devList[i]));
	}

	return jDevList;
}

JNIEXPORT jboolean JNICALL Java_org_meltzg_jmtp_JMTP_selectDevice
(JNIEnv *env, jobject obj, jstring id) {
	PWSTR cId = jStringToWchar(env, id);
	auto device = getSelectedDevice(cId);
	delete[] cId;
	return device != nullptr;
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmtp_JMTP_getDeviceContent
(JNIEnv *env, jobject obj) {
	MTPObjectTree *content = getDeviceContent();
	jobject jContent = mtpotToJMtpot(env, content);
	delete content;
	return jContent;
}

JNIEXPORT jstring JNICALL Java_org_meltzg_jmtp_JMTP_transferToDevice
(JNIEnv *env, jobject obj, jstring filepath, jstring destId, jstring destName) {
	PWSTR cFilepath = jStringToWchar(env, filepath);
	PWSTR cDestId = jStringToWchar(env, destId);
	PWSTR cDestName = jStringToWchar(env, destName);

	wstring newId = transferToDevice(cFilepath, cDestId, cDestName);

	delete[] cFilepath;
	delete[] cDestId;
	delete[] cDestName;

	if (newId.empty()) {
		return nullptr;
	}
	else {
		return wcharToJString(env, newId.c_str());
	}
}

JNIEXPORT jboolean JNICALL Java_org_meltzg_jmtp_JMTP_removeFromDevice
(JNIEnv *env, jobject obj, jstring id, jstring stopId) {
	PWSTR cId = jStringToWchar(env, id);
	PWSTR cStopId = jStringToWchar(env, stopId);

	bool ret = removeFromDevice(cId, cStopId);

	delete[] cId;
	delete[] cStopId;

	return ret;
}

JNIEXPORT jboolean JNICALL Java_org_meltzg_jmtp_JMTP_transferFromDevice
(JNIEnv *env, jobject obj, jstring id, jstring destFilepath) {
	PWSTR cId = jStringToWchar(env, id);
	PWSTR cDestFilepath = jStringToWchar(env, destFilepath);

	bool ret = transferFromDevice(cId, cDestFilepath);

	delete[] cId;
	delete[] cDestFilepath;

	return ret;
}

JNIEXPORT jlong JNICALL Java_org_meltzg_jmtp_JMTP_initCOM
(JNIEnv *env, jobject obj) {

	return initCOM();
}

JNIEXPORT void JNICALL Java_org_meltzg_jmtp_JMTP_closeCOM
(JNIEnv *env, jobject obj) {
	closeCOM();
}