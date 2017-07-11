#include <jni.h>
#include <iostream>
#include <string>
#include <sstream>
#include <PortableDeviceApi.h>
#include <wrl/client.h>

#include "../../jMTP/bin/org_meltzg_jmtp_JMTP.h"

#define JSTRING "Ljava/lang/String;"
#define JARRLIST "Ljava/util/ArrayList;"
#define JMTPDEV "Lorg/meltzg/jmtp/mtp/MTPDevice;"

using std::cout;
using std::cerr;
using std::endl;
using std::wcout;
using std::hex;
using std::string;
using std::wstring;
using std::ostringstream;
using std::nothrow;

using Microsoft::WRL::ComPtr;

ComPtr<IPortableDeviceManager> getDeviceManager();
string formatHR(HRESULT hr);
PWSTR getDeviceDescription(PWSTR deviceId);
PWSTR getDeviceFriendlyName(PWSTR deviceId);
PWSTR getDeviceManufacturer(PWSTR deviceId);
jstring wcharToJString(wchar_t* wstr, JNIEnv *env);
jobject getNewArrayList(JNIEnv *env);
void arrayListAdd(JNIEnv *env, jobject list, jobject element);
jobject getNewMTPDevice(JNIEnv *env, PWSTR devId, PWSTR devFName, PWSTR devDesc, PWSTR devManu);

JNIEXPORT void JNICALL Java_org_meltzg_jmtp_JMTP_sayHello
(JNIEnv *env, jobject obj) {
	cout << "Hello World" << endl;
}

JNIEXPORT jstring JNICALL Java_org_meltzg_jmtp_JMTP_getHello
(JNIEnv *env, jobject obj) {
	PWSTR hello = L"hello world";
	return wcharToJString(hello, env);
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmtp_JMTP_getDevices
(JNIEnv *env, jobject obj) {
	auto deviceManager = getDeviceManager();
	jobject jDevList = getNewArrayList(env);

	if (deviceManager != nullptr) {
		DWORD deviceCnt = 0;

		HRESULT hr = deviceManager->GetDevices(nullptr, &deviceCnt);
		if (FAILED(hr)) {
			cerr << "!!! Unable to get device count: " << formatHR(hr) << endl;
		}
		else {
			cout << "# Devices found: " << deviceCnt << endl;
			if (SUCCEEDED(hr) && deviceCnt > 0) {
				PWSTR * deviceIds = new (nothrow) PWSTR[deviceCnt];
				hr = deviceManager->GetDevices(deviceIds, &deviceCnt);

				if (SUCCEEDED(hr)) {
					for (unsigned int i = 0; i < deviceCnt; i++) {
						wcout << "\t- ID: " << deviceIds[i] << endl;

						PWSTR dDesc = getDeviceDescription(deviceIds[i]);
						PWSTR dFName = getDeviceFriendlyName(deviceIds[i]);
						PWSTR dManuf = getDeviceManufacturer(deviceIds[i]);

						wcout << "\t\t- Description: " << dDesc << endl;
						wcout << "\t\t- Friendly name: " << dFName << endl;
						wcout << "\t\t- Manufacturer: " << dManuf << endl;

						// add to arraylist
						jobject mtpd = getNewMTPDevice(env, deviceIds[i], dFName, dDesc, dManuf);
						arrayListAdd(env, jDevList, mtpd);

						delete[] dDesc;
						delete[] dFName;
						delete[] dManuf;
					}
				}
				else {
					cerr << "!!! Failed to retrieve device IDs: " << formatHR(hr) << endl;
				}

				for (unsigned int i = 0; i < deviceCnt; i++) {
					CoTaskMemFree(deviceIds[i]);
					deviceIds[i] = NULL;
				}
				delete[] deviceIds;
				deviceIds = nullptr;
			}
		}

		return jDevList;
	}

}

JNIEXPORT jlong JNICALL Java_org_meltzg_jmtp_JMTP_initCOM
(JNIEnv *env, jobject obj) {

	static HRESULT hr = E_FAIL;

	if (FAILED(hr)) {
		hr = CoInitializeEx(nullptr, COINIT_MULTITHREADED);

		if (FAILED(hr)) {
			cerr << "!!! Failed to CoInitialize: " << formatHR(hr) << endl;
		}
		else {
			cout << "COM initialized" << endl;
		}
	}

	return hr;
}

JNIEXPORT void JNICALL Java_org_meltzg_jmtp_JMTP_closeCOM
(JNIEnv *env, jobject obj) {
	CoUninitialize();
	cout << "COM closed" << endl;
}

ComPtr<IPortableDeviceManager> getDeviceManager() {

	static ComPtr<IPortableDeviceManager> deviceManager = nullptr;

	if (deviceManager == nullptr) {
		HRESULT hr = CoCreateInstance(
			CLSID_PortableDeviceManager,
			nullptr,
			CLSCTX_INPROC_SERVER,
			IID_PPV_ARGS(&deviceManager));

		if (FAILED(hr)) {
			cerr << "!!! Failed to get CoCreateInstance: " << formatHR(hr) << endl;
			deviceManager = nullptr;
		}
		else {
			cout << "Device Manager initialized" << endl;
		}
	}

	return deviceManager;
}

string formatHR(HRESULT hr) {
	ostringstream strStream;

	strStream << "hr=0x" << hex << hr;
	return strStream.str();
}

PWSTR getDeviceDescription(PWSTR deviceId) {
	DWORD descLength = 0;
	PWSTR description = nullptr;
	auto deviceManager = getDeviceManager();

	HRESULT hr = deviceManager->GetDeviceDescription(deviceId, nullptr, &descLength);
	if (FAILED(hr)) {
		cerr << "!!! Failed to get device description length for ID=" << deviceId << ", " << formatHR(hr) << endl;
	}
	else if (descLength > 0) {
		description = new (nothrow) WCHAR[descLength];
		hr = deviceManager->GetDeviceDescription(deviceId, description, &descLength);

		if (FAILED(hr)) {
			delete[] description;
			description = nullptr;
			cerr << "!!! Failed to get device description for ID=" << deviceId << ", " << formatHR(hr) << endl;
		}
	}

	return description;
}

PWSTR getDeviceFriendlyName(PWSTR deviceId) {
	DWORD fNameLength = 0;
	PWSTR friendlyName = nullptr;
	auto deviceManager = getDeviceManager();

	HRESULT hr = deviceManager->GetDeviceFriendlyName(deviceId, nullptr, &fNameLength);
	if (FAILED(hr)) {
		cerr << "!!! Failed to get device friendly name length for ID=" << deviceId << ", " << formatHR(hr) << endl;
	}
	else if (fNameLength > 0) {
		friendlyName = new (nothrow) WCHAR[fNameLength];
		hr = deviceManager->GetDeviceFriendlyName(deviceId, friendlyName, &fNameLength);

		if (FAILED(hr)) {
			delete[] friendlyName;
			friendlyName = nullptr;
			cerr << "!!! Failed to get device friendly name for ID=" << deviceId << ", " << formatHR(hr) << endl;
		}
	}

	return friendlyName;
}

PWSTR getDeviceManufacturer(PWSTR deviceId) {
	DWORD manuLength = 0;
	PWSTR manufacturer = nullptr;
	auto deviceManager = getDeviceManager();

	HRESULT hr = deviceManager->GetDeviceManufacturer(deviceId, nullptr, &manuLength);
	if (FAILED(hr)) {
		cerr << "!!! Failed to get device manufacturer length for ID=" << deviceId << ", " << formatHR(hr) << endl;
	}
	else if (manuLength > 0) {
		manufacturer = new (nothrow) WCHAR[manuLength];
		hr = deviceManager->GetDeviceManufacturer(deviceId, manufacturer, &manuLength);

		if (FAILED(hr)) {
			delete[] manufacturer;
			manufacturer = nullptr;
			cerr << "!!! Failed to get device manufacturer for ID=" << deviceId << ", " << formatHR(hr) << endl;
		}
	}

	return manufacturer;
}

jstring wcharToJString(wchar_t *wstr, JNIEnv *env) {
	size_t origSize = wcslen(wstr) + 1;
	size_t convertedChars = 0;
	const size_t newSize = origSize * 2;
	char* newString = new char[newSize];

	wcstombs_s(&convertedChars, newString, newSize, wstr, _TRUNCATE);
	jstring ret = env->NewStringUTF(newString);
	delete newString;

	return ret;
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

jobject getNewMTPDevice(JNIEnv *env, PWSTR devId, PWSTR devFName, PWSTR devDesc, PWSTR devManu) {
	jclass mtpDeviceClass = env->FindClass(JMTPDEV);
	
	// (js, js, js)V
	string sig = "(";
	sig += JSTRING;
	sig += JSTRING;
	sig += JSTRING;
	sig += JSTRING;
	sig += ")V";

	jmethodID mtpDeviceConstructor = env->GetMethodID(mtpDeviceClass, "<init>", sig.c_str());

	jstring jDevId = wcharToJString(devId, env);
	jstring jFName = wcharToJString(devFName, env);
	jstring jDDesc = wcharToJString(devDesc, env);
	jstring jDManu = wcharToJString(devManu, env);

	return env->NewObject(mtpDeviceClass, mtpDeviceConstructor, jDevId, jFName, jDDesc, jDManu);
}