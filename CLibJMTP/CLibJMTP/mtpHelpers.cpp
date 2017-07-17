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

ComPtr<IPortableDeviceValues> getClientInfo()
{
	static ComPtr<IPortableDeviceValues> info = nullptr;

	if (info == nullptr) {
		HRESULT hr = CoCreateInstance(CLSID_PortableDeviceValues,
			nullptr,
			CLSCTX_INPROC_SERVER,
			IID_PPV_ARGS(&info));

		if (FAILED(hr)) {
			info = nullptr;
			cerr << "!!! Failed to CoCreateInstance CLSID_PortableDeviceValues: " << formatHR(hr) << endl;
		}
		else {
			hr = info->SetUnsignedIntegerValue(WPD_CLIENT_SECURITY_QUALITY_OF_SERVICE, SECURITY_IMPERSONATION);
			if (FAILED(hr))
			{
				info = nullptr;
				cerr << "!!! Failed to set WPD_CLIENT_SECURITY_QUALITY_OF_SERVICE: " << formatHR(hr);
			}
		}
	}

	return info;
}

ComPtr<IPortableDevice> getSelectedDevice(PWSTR id)
{
	static ComPtr<IPortableDevice> device = nullptr;
	auto manager = getDeviceManager();
	auto info = getClientInfo();

	if ((device == nullptr || id != nullptr) && manager != nullptr && info != nullptr) {
		HRESULT hr = CoCreateInstance(CLSID_PortableDeviceFTM,
			nullptr,
			CLSCTX_INPROC_SERVER,
			IID_PPV_ARGS(&device));

		if (FAILED(hr)) {
			device = nullptr;
			cerr << "!!! Failed to CoCreateInstance CLSID_PortableDeviceFTM: " << formatHR(hr) << endl;
		}
		else {
			hr = device->Open(id, info.Get());
			if (FAILED(hr)) {
				device = nullptr;
				cerr << "!!! Failed to open device: " << formatHR(hr) << endl;
			}
		}
	}

	return device;
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
			if (SUCCEEDED(hr) && deviceCnt > 0) {
				PWSTR * deviceIds = new (nothrow) PWSTR[deviceCnt];
				hr = deviceManager->GetDevices(deviceIds, &deviceCnt);

				if (SUCCEEDED(hr)) {
					for (unsigned int i = 0; i < deviceCnt; i++) {

						PWSTR dDesc = getDeviceDescription(deviceIds[i]);
						PWSTR dFName = getDeviceFriendlyName(deviceIds[i]);
						PWSTR dManuf = getDeviceManufacturer(deviceIds[i]);

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

MTPObjectTree getDeviceContent(PWSTR rootId, IPortableDeviceContent *content) {
	MTPObjectTree oTree(rootId);

	ComPtr<IEnumPortableDeviceObjectIDs> objIdsEnum;
	HRESULT hr = content->EnumObjects(0, rootId, nullptr, &objIdsEnum);

	//std::wcout << rootId << endl;

	if (FAILED(hr)) {
		cerr << "!!! Failed to get IEnumPortableDeviceObjectIDs: " << formatHR(hr) << endl;
	}

	while (hr == S_OK) {
		DWORD fetched = 0;
		PWSTR objIds[NUM_OBJECTS_TO_REQUEST] = { nullptr };
		hr = objIdsEnum->Next(NUM_OBJECTS_TO_REQUEST,
			objIds,
			&fetched);

		if (SUCCEEDED(hr)) {
			for (DWORD i = 0; i < fetched; i++) {
				oTree.children.push_back(getDeviceContent(objIds[i], content));
			}
		}
	}

	return oTree;
}

MTPObjectTree getDeviceContent()
{
	auto device = getSelectedDevice(NULL);
	MTPObjectTree oTree;
	ComPtr<IPortableDeviceContent> content = nullptr;

	if (device != nullptr) {
		HRESULT hr = device->Content(&content);
		if (FAILED(hr)) {
			cerr << "!!! Failed to get IPortableDeviceContent: " << formatHR(hr) << endl;
		}
		else {
			oTree = getDeviceContent(WPD_DEVICE_OBJECT_ID, content.Get());
		}
	}

	return oTree;
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
