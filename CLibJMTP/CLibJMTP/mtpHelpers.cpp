#include <iostream>
#include<sstream>

#include "mtpHelpers.h"

using std::cerr;
using std::endl;
using std::nothrow;
using std::ostringstream;
using std::hex;

HRESULT initCOM() {
	static HRESULT hr = E_FAIL;

	if (FAILED(hr)) {
		hr = CoInitializeEx(nullptr, COINIT_MULTITHREADED);

		if (FAILED(hr)) {
			cerr << "!!! Failed to CoInitialize: " << formatHR(hr) << endl;
		}
		else {
			//cout << "COM initialized" << endl;
		}
	}

	return hr;
}

void closeCOM() {
	CoUninitialize();
	//cout << "COM closed" << endl;
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
			//cout << "Device Manager initialized" << endl;
		}
	}

	return deviceManager;
}

vector<MTPDevice> getDevices()
{
	auto deviceManager = getDeviceManager();
	vector<MTPDevice> devList;

	if (deviceManager != nullptr) {
		DWORD deviceCnt = 0;

		HRESULT hr = deviceManager->GetDevices(nullptr, &deviceCnt);
		if (FAILED(hr)) {
			cerr << "!!! Unable to get device count: " << formatHR(hr) << endl;
		}
		else {
			//cout << "# Devices found: " << deviceCnt << endl;
			if (SUCCEEDED(hr) && deviceCnt > 0) {
				PWSTR * deviceIds = new (nothrow) PWSTR[deviceCnt];
				hr = deviceManager->GetDevices(deviceIds, &deviceCnt);

				if (SUCCEEDED(hr)) {
					for (unsigned int i = 0; i < deviceCnt; i++) {
						//wcout << "\t- ID: " << deviceIds[i] << endl;

						PWSTR dDesc = getDeviceDescription(deviceIds[i]);
						PWSTR dFName = getDeviceFriendlyName(deviceIds[i]);
						PWSTR dManuf = getDeviceManufacturer(deviceIds[i]);

						//wcout << "\t\t- Description: " << dDesc << endl;
						//wcout << "\t\t- Friendly name: " << dFName << endl;
						//wcout << "\t\t- Manufacturer: " << dManuf << endl;

						// add to arraylist
						MTPDevice mtpd(deviceIds[i], dDesc, dFName, dManuf);
						devList.push_back(mtpd);

						delete[] dDesc;
						delete[] dFName;
						delete[] dManuf;
					}

					for (unsigned int i = 0; i < deviceCnt; i++) {
						CoTaskMemFree(deviceIds[i]);
						deviceIds[i] = nullptr;
					}
				}
				else {
					cerr << "!!! Failed to retrieve device IDs: " << formatHR(hr) << endl;
				}

				delete[] deviceIds;
				deviceIds = nullptr;
			}
		}
	}

	return devList;
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