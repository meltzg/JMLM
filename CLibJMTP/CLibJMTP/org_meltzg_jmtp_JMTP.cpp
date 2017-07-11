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
		arrayListAdd(env, jDevList, mtpdToJMptd(env, devList[i]));
	}

	return jDevList;
}

JNIEXPORT jlong JNICALL Java_org_meltzg_jmtp_JMTP_initCOM
(JNIEnv *env, jobject obj) {

	return initCOM();
}

JNIEXPORT void JNICALL Java_org_meltzg_jmtp_JMTP_closeCOM
(JNIEnv *env, jobject obj) {
	closeCOM();
}