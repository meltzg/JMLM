#include <string>
#include "jniHelpers.h"

using std::string;
using std::wstring;

jstring wcharToJString(JNIEnv *env, wchar_t *wstr) {
	size_t origSize = wcslen(wstr) + 1;
	size_t convertedChars = 0;
	const size_t newSize = origSize * 2;
	char* newString = new char[newSize];

	wcstombs_s(&convertedChars, newString, newSize, wstr, _TRUNCATE);
	jstring ret = env->NewStringUTF(newString);
	delete newString;

	return ret;
}

wchar_t* jStringToWchar(JNIEnv *env, jstring jStr)
{
	const jchar *raw = env->GetStringChars(jStr, 0);
	jsize len = env->GetStringLength(jStr);
	wstring wStr;
	wchar_t *wStr_c = new wchar_t[len + 1];

	wStr.assign(raw, raw + len);
	wcscpy_s(wStr_c, len + 1, wStr.c_str());

	return wStr_c;
}

jobject mtpdToJMptd(JNIEnv *env, MTPDevice mtpd)
{
	return getNewMTPDevice(env,
		mtpd.getId(),
		mtpd.getFriendlyName(),
		mtpd.getDescription(),
		mtpd.getManufacturer());
}

jobject getNewArrayList(JNIEnv *env) {
	jclass arrayListClass = env->FindClass(JARRLIST);
	jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
	return env->NewObject(arrayListClass, arrayListConstructor);
}

void arrayListAdd(JNIEnv *env, jobject list, jobject element) {
	jclass arrayListClass = env->FindClass(JARRLIST);
	jmethodID arrayListAdd = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
	env->CallBooleanMethod(list, arrayListAdd, element);
}

jobject getNewMTPDevice(JNIEnv *env, wchar_t *devId, wchar_t *devFName, wchar_t *devDesc, wchar_t *devManu) {
	jclass mtpDeviceClass = env->FindClass(JMTPDEV);

	// (js, js, js)V
	string sig = "(";
	sig += JSTRING;
	sig += JSTRING;
	sig += JSTRING;
	sig += JSTRING;
	sig += ")V";

	jmethodID mtpDeviceConstructor = env->GetMethodID(mtpDeviceClass, "<init>", sig.c_str());

	jstring jDevId = wcharToJString(env, devId);
	jstring jFName = wcharToJString(env, devFName);
	jstring jDDesc = wcharToJString(env, devDesc);
	jstring jDManu = wcharToJString(env, devManu);

	return env->NewObject(mtpDeviceClass, mtpDeviceConstructor, jDevId, jFName, jDDesc, jDManu);
}