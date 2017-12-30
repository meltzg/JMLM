#include <iostream>
#include <sstream>
#include <PortableDeviceApi.h>
#include <PortableDevice.h>
#include <wrl/client.h>
#include <shlwapi.h>

#include "mtpHelpers.h"

using std::cerr;
using std::endl;
using std::string;
using std::ostringstream;
using std::hex;
using std::nothrow;

using Microsoft::WRL::ComPtr;

namespace LibJMTP {

	string formatHR(HRESULT hr) {
		ostringstream strStream;

		strStream << "hr=0x" << hex << hr;
		return strStream.str();
	}

	void logErr(char * msg, HRESULT hr) {
		cerr << msg << formatHR(hr) << hex << endl;
	}

	HRESULT initCOM() {
		HRESULT hr = CoInitializeEx(nullptr, COINIT_MULTITHREADED);
		if (FAILED(hr)) {
			logErr("!!! Failed to CoInitializeEx: ", hr);
		}
		return hr;
	}

	void closeCOM() {
		CoUninitialize();
	}

	wstring getDeviceDescription(IPortableDeviceManager *deviceManager, PWSTR deviceId) {
		DWORD descLength = 0;
		PWSTR description = nullptr;

		if (deviceId != nullptr) {

			HRESULT hr = deviceManager->GetDeviceDescription(deviceId, nullptr, &descLength);
			if (FAILED(hr)) {
				logErr("!!! Failed to get device description length: ", hr);
			}
			else if (descLength > 0) {
				description = new (nothrow) WCHAR[descLength];
				hr = deviceManager->GetDeviceDescription(deviceId, description, &descLength);

				if (FAILED(hr)) {
					delete[] description;
					description = nullptr;
					logErr("!!! Failed to get device description: ", hr);
				}
			}
		}

		wstring ret(description);
		delete[] description;

		return ret;
	}

	wstring getDeviceFriendlyName(IPortableDeviceManager *deviceManager, PWSTR deviceId) {
		DWORD fNameLength = 0;
		PWSTR friendlyName = nullptr;

		if (deviceId != nullptr) {

			HRESULT hr = deviceManager->GetDeviceFriendlyName(deviceId, nullptr, &fNameLength);
			if (FAILED(hr)) {
				logErr("!!! Failed to get device friendly name length: ", hr);
			}
			else if (fNameLength > 0) {
				friendlyName = new (nothrow) WCHAR[fNameLength];
				hr = deviceManager->GetDeviceFriendlyName(deviceId, friendlyName, &fNameLength);

				if (FAILED(hr)) {
					delete[] friendlyName;
					friendlyName = nullptr;
					logErr("!!! Failed to get device friendly name: ", hr);
				}
			}
		}

		wstring ret(friendlyName);
		delete[] friendlyName;

		return ret;
	}

	wstring getDeviceManufacturer(IPortableDeviceManager *deviceManager, PWSTR deviceId) {
		DWORD manuLength = 0;
		PWSTR manufacturer = nullptr;

		if (deviceId != nullptr) {

			HRESULT hr = deviceManager->GetDeviceManufacturer(deviceId, nullptr, &manuLength);
			if (FAILED(hr)) {
				logErr("!!! Failed to get device manufacturer length: ", hr);
			}
			else if (manuLength > 0) {
				manufacturer = new (nothrow) WCHAR[manuLength];
				hr = deviceManager->GetDeviceManufacturer(deviceId, manufacturer, &manuLength);

				if (FAILED(hr)) {
					delete[] manufacturer;
					manufacturer = nullptr;
					logErr("!!! Failed to get device manufacturer: ", hr);
				}
			}
		}

		wstring ret(manufacturer);
		delete[] manufacturer;

		return ret;
	}

	vector<MTPDeviceInfo> getDevicesInfo() {
		vector<MTPDeviceInfo> devices;
		if (SUCCEEDED(initCOM())) {
			ComPtr<IPortableDeviceManager> deviceManager;
			DWORD deviceCount = 0;

			HRESULT hr = CoCreateInstance(
				CLSID_PortableDeviceManager,
				NULL,
				CLSCTX_INPROC_SERVER,
				IID_PPV_ARGS(&deviceManager));

			if (FAILED(hr)) {
				logErr("!!! Failed to CoInstanceCreate CLSID_PortableDeviceManager: ", hr);
			}
			else {
				hr = deviceManager->GetDevices(NULL, &deviceCount);
			}

			if (SUCCEEDED(hr) && deviceCount > 0) {
				PWSTR *deviceIDs = new(nothrow) PWSTR[deviceCount];
				if (deviceIDs != nullptr) {
					hr = deviceManager->GetDevices(deviceIDs, &deviceCount);
					if (SUCCEEDED(hr)) {
						for (unsigned int i = 0; i < deviceCount; i++) {
							MTPDeviceInfo info;
							info.deviceId = deviceIDs[i];
							info.description = getDeviceDescription(deviceManager.Get(), deviceIDs[i]);
							info.friendlyName = getDeviceFriendlyName(deviceManager.Get(), deviceIDs[i]);
							info.manufacturer = getDeviceManufacturer(deviceManager.Get(), deviceIDs[i]);

							devices.push_back(info);
							CoTaskMemFree(deviceIDs[i]);
							deviceIDs[i] = nullptr;
						}
						delete[] deviceIDs;
						deviceIDs = nullptr;
					}
					else {
						logErr("!!! Failed to retrieve device list from system: ", hr);
					}
				}
			}
			deviceManager.Reset();
			closeCOM();
		}
		return devices;
	}
	
	vector<wstring> getChildIds(wstring deviceId, wstring parentId)
	{
		return vector<wstring>();
	}

	MTPContentNode createDirNode(wstring deviceId, wstring parentId, wstring name)
	{
		return MTPContentNode();
	}
	MTPContentNode createContentNode(wstring deviceId, wstring parentId, wstring file)
	{
		return MTPContentNode();
	}
	MTPContentNode readNode(wstring deviceId, wstring id)
	{
		return MTPContentNode();
	}
	MTPContentNode copyNode(wstring deviceId, wstring parentId, wstring id, wstring tmpFolder)
	{
		return MTPContentNode();
	}
	bool deleteNode(wstring deviceId, wstring id)
	{
		return false;
	}
	bool retrieveNode(wstring deviceId, wstring id, wstring destFolder)
	{
		return false;
	}
}