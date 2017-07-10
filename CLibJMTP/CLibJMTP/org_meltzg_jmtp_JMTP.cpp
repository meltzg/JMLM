#include <jni.h>
#include <iostream>
#include <string>
#include <sstream>
#include <PortableDeviceApi.h>
#include <wrl/client.h>

#include "../../jMTP/bin/org_meltzg_jmtp_JMTP.h"

using std::cout;
using std::cerr;
using std::endl;
using std::hex;
using std::string;
using std::ostringstream;

using Microsoft::WRL::ComPtr;

ComPtr<IPortableDeviceManager> getDeviceManager();
string formatHR(HRESULT hr);

JNIEXPORT void JNICALL Java_org_meltzg_jmtp_JMTP_sayHello
(JNIEnv *env, jobject obj) {
	cout << "Hello World" << endl;
}

JNIEXPORT void JNICALL Java_org_meltzg_jmtp_JMTP_getDevices
(JNIEnv *env, jobject obj) {
	auto deviceManager = getDeviceManager();

	if (deviceManager != nullptr) {
		DWORD deviceCnt = 0;

		HRESULT hr = deviceManager->GetDevices(nullptr, &deviceCnt);
		if (FAILED(hr)) {
			cerr << "!!! Unable to get device count: " << formatHR(hr) << endl;
			return;
		}

		cout << "# Devices found: " << deviceCnt << endl;
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