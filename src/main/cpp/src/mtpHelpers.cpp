#include <iostream>
#include <sstream>
#include <PortableDeviceApi.h>
#include <PortableDevice.h>
#include <wrl/client.h>
#include <shlwapi.h>
#include <guiddef.h>

#include "mtpHelpers.h"
#include "commonHelpers.h"

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

	void logErr(const char * msg, HRESULT hr) {
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

	HRESULT getDeviceManager(ComPtr<IPortableDeviceManager> &deviceManager) {
		HRESULT hr = CoCreateInstance(
			CLSID_PortableDeviceManager,
			NULL,
			CLSCTX_INPROC_SERVER,
			IID_PPV_ARGS(&deviceManager));

		if (FAILED(hr)) {
			logErr("!!! Failed to CoInstanceCreate CLSID_PortableDeviceManager: ", hr);
		}

		return hr;
	}

	HRESULT getClientInfo(ComPtr<IPortableDeviceValues> &clientInfo) {
		HRESULT hr = CoCreateInstance(CLSID_PortableDeviceValues,
			nullptr,
			CLSCTX_INPROC_SERVER,
			IID_PPV_ARGS(&clientInfo));

		if (FAILED(hr)) {
			clientInfo = nullptr;
			logErr("!!! Failed to CoCreateInstance CLSID_PortableDeviceValues: ", hr);
		}
		else {
			hr = clientInfo->SetUnsignedIntegerValue(WPD_CLIENT_SECURITY_QUALITY_OF_SERVICE, SECURITY_IMPERSONATION);
			if (FAILED(hr))
			{
				clientInfo = nullptr;
				logErr("!!! Failed to set WPD_CLIENT_SECURITY_QUALITY_OF_SERVICE: ", hr);
			}
		}

		return hr;
	}

	HRESULT getPortableDevice(ComPtr<IPortableDevice> &device, wstring id) {
		ComPtr<IPortableDeviceValues> clientInfo;
		HRESULT hr = getClientInfo(clientInfo);
		
		if (SUCCEEDED(hr)) {
			hr = CoCreateInstance(CLSID_PortableDeviceFTM,
				nullptr,
				CLSCTX_INPROC_SERVER,
				IID_PPV_ARGS(&device));

			if (FAILED(hr)) {
				device = nullptr;
				logErr("!!! Failed to CoCreateInstance CLSID_PortableDeviceFTM: ", hr);
			}
			else {
				hr = device->Open(id.c_str(), clientInfo.Get());
				if (FAILED(hr)) {
					device = nullptr;
					logErr("!!! Failed to open device: ", hr);
				}
			}
		}
		
		clientInfo.Reset();
		return hr;
	}

	wstring getDeviceDescription(IPortableDeviceManager *deviceManager, const wchar_t * deviceId) {
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

	wstring getDeviceFriendlyName(IPortableDeviceManager *deviceManager, const wchar_t * deviceId) {
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

	wstring getDeviceManufacturer(IPortableDeviceManager *deviceManager, const wchar_t *deviceId) {
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

	MTPDeviceInfo getDeviceInfo(IPortableDeviceManager *deviceManager, wstring id)
	{
		MTPDeviceInfo info;
		info.deviceId = id.c_str();
		info.description = getDeviceDescription(deviceManager, id.c_str());
		info.friendlyName = getDeviceFriendlyName(deviceManager, id.c_str());
		info.manufacturer = getDeviceManufacturer(deviceManager, id.c_str());

		return info;
	}

	void addToKeyCollection(IPortableDeviceKeyCollection *propsToRead, REFPROPERTYKEY key, char *name) {
		HRESULT hr = propsToRead->Add(key);
		if (FAILED(hr)) {
			ostringstream message;
			message << "!!! Failed to add " << name << " to IPortableDeviceKeyCollection: ";

			logErr(message.str().c_str(), hr);
		}
	}

	void getStringProperty(IPortableDeviceValues * values, REFPROPERTYKEY key, PWSTR * destination)
	{
		PWSTR value = nullptr;
		HRESULT hr = values->GetStringValue(key, &value);

		if (SUCCEEDED(hr)) {
			wcsAllocCpy(destination, value);
		}

		CoTaskMemFree(value);
		value = nullptr;
	}

	void getULongLongProperty(IPortableDeviceValues * values, REFPROPERTYKEY key, ULONGLONG * destination)
	{
		ULONGLONG value = 0;
		HRESULT hr = values->GetUnsignedLargeIntegerValue(key, &value);

		if (SUCCEEDED(hr)) {
			*destination = value;
		}
	}

	void getGUIDProperty(IPortableDeviceValues * values, REFPROPERTYKEY key, GUID * destination) {
		HRESULT hr = values->GetGuidValue(key, destination);
	}

	vector<MTPDeviceInfo> getDevicesInfo() {
		vector<MTPDeviceInfo> devices;
		if (SUCCEEDED(initCOM())) {
			ComPtr<IPortableDeviceManager> deviceManager;
			DWORD deviceCount = 0;

			HRESULT hr = getDeviceManager(deviceManager);

			if (SUCCEEDED(hr)) {
				hr = deviceManager->GetDevices(NULL, &deviceCount);
			}

			if (SUCCEEDED(hr) && deviceCount > 0) {
				PWSTR *deviceIDs = new(nothrow) PWSTR[deviceCount];
				if (deviceIDs != nullptr) {
					hr = deviceManager->GetDevices(deviceIDs, &deviceCount);
					if (SUCCEEDED(hr)) {
						for (unsigned int i = 0; i < deviceCount; i++) {
							MTPDeviceInfo info = getDeviceInfo(deviceManager.Get(), deviceIDs[i]);

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

	MTPDeviceInfo getDeviceInfo(wstring id)
	{
		MTPDeviceInfo info;

		if (SUCCEEDED(initCOM())) {
			ComPtr<IPortableDeviceManager> deviceManager;

			HRESULT hr = getDeviceManager(deviceManager);

			if (SUCCEEDED(hr)) {
				info = getDeviceInfo(deviceManager.Get(), id.c_str());
			}
			deviceManager.Reset();
			closeCOM();
		}

		return info;
	}
	
	vector<wstring> getChildIds(wstring deviceId, wstring parentId)
	{
		ComPtr<IPortableDevice> device = nullptr;
		ComPtr<IPortableDeviceContent> content = nullptr;
		ComPtr<IEnumPortableDeviceObjectIDs> objIdsEnum = nullptr;

		vector<wstring> childIds;

		if (SUCCEEDED(initCOM())) {
			HRESULT hr = getPortableDevice(device, deviceId);

			if (SUCCEEDED(hr)) {
				hr = device->Content(&content);
				if (FAILED(hr)) {
					logErr("!!! Failed to get IPortbleDeviceContent: ", hr);
				}
			}

			if (SUCCEEDED(hr)) {
				hr = content->EnumObjects(0, parentId.c_str(), nullptr, &objIdsEnum);
				if (FAILED(hr)) {
					logErr("!!! Failed to get IEnumPortableDeviceObjectIDs: ", hr);
				}
			}

			while (hr == S_OK) {
				DWORD fetched = 0;
				PWSTR objIds[NUM_OBJECTS_TO_REQUEST] = { nullptr };
				hr = objIdsEnum->Next(NUM_OBJECTS_TO_REQUEST,
					objIds,
					&fetched);

				if (SUCCEEDED(hr)) {
					for (DWORD i = 0; i < fetched; i++) {
						childIds.push_back(wstring(objIds[i]));
						CoTaskMemFree(objIds[i]);
					}
				}
			}
			device.Reset();
			content.Reset();
			objIdsEnum.Reset();

			closeCOM();
		}

		return childIds;
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
		ComPtr<IPortableDevice> device = nullptr;
		ComPtr<IPortableDeviceContent> content = nullptr;
		ComPtr<IPortableDeviceValues> objVals = nullptr;
		ComPtr<IPortableDeviceProperties> props = nullptr;
		ComPtr<IPortableDeviceKeyCollection> propsToRead = nullptr;

		MTPContentNode node;
		node.id = id;
		node.isValid = false;

		if (SUCCEEDED(initCOM())) {
			HRESULT hr = getPortableDevice(device, deviceId);
			if (SUCCEEDED(hr)) {
				hr = device->Content(&content);
				if (FAILED(hr)) {
					logErr("!!! Failed to get IPortbleDeviceContent: ", hr);
				}
			}

			if (SUCCEEDED(hr)) {
				hr = content->Properties(&props);
				if (FAILED(hr)) {
					logErr("!!! Failed to get IPortableDeviceProperties: ", hr);
				}
			}

			if (SUCCEEDED(hr)) {
				hr = CoCreateInstance(CLSID_PortableDeviceKeyCollection,
					nullptr,
					CLSCTX_INPROC_SERVER,
					IID_PPV_ARGS(&propsToRead));
				if (FAILED(hr)) {
					logErr("!!! Failed to CoCreateInstance CLSID_PortableDeviceKeyCollection: ", hr);
				}
			}

			if (SUCCEEDED(hr)) {
				addToKeyCollection(propsToRead.Get(), WPD_OBJECT_PARENT_ID, "WPD_OBJECT_PARENT_ID");
				addToKeyCollection(propsToRead.Get(), WPD_OBJECT_NAME, "WPD_OBJECT_NAME");
				addToKeyCollection(propsToRead.Get(), WPD_OBJECT_ORIGINAL_FILE_NAME, "WPD_OBJECT_ORIGINAL_FILE_NAME");
				addToKeyCollection(propsToRead.Get(), WPD_OBJECT_SIZE, "WPD_OBJECT_SIZE");
				addToKeyCollection(propsToRead.Get(), WPD_STORAGE_CAPACITY, "WPD_STORAGE_CAPACITY");
				addToKeyCollection(propsToRead.Get(), WPD_OBJECT_CONTENT_TYPE, "WPD_OBJECT_CONTENT_TYPE");

				hr = props->GetValues(id.c_str(),
					propsToRead.Get(),
					&objVals);
				if (FAILED(hr)) {
					logErr("!!! Failed to get all properties for object: ", hr);
				}
			}

			if (SUCCEEDED(hr)) {
				wchar_t * parentId = nullptr;
				wchar_t * name = nullptr;
				wchar_t * origName = nullptr;
				unsigned long long size = 0;
				unsigned long long capacity = 0;
				GUID contentType;

				getStringProperty(objVals.Get(), WPD_OBJECT_PARENT_ID, &parentId);
				getStringProperty(objVals.Get(), WPD_OBJECT_NAME, &name);
				getStringProperty(objVals.Get(), WPD_OBJECT_ORIGINAL_FILE_NAME, &origName);
				getULongLongProperty(objVals.Get(), WPD_OBJECT_SIZE, &size);
				getULongLongProperty(objVals.Get(), WPD_STORAGE_CAPACITY, &capacity);
				getGUIDProperty(objVals.Get(), WPD_OBJECT_CONTENT_TYPE, &contentType);

				node.pId = wstring(parentId ? parentId : L"");
				node.name = wstring(name ? name : L"");
				node.origName = wstring(origName ? origName : L"");
				node.capacity = capacity;
				node.isValid = true;

				node.isDir = IsEqualGUID(contentType, WPD_CONTENT_TYPE_FUNCTIONAL_OBJECT) ||
					IsEqualGUID(contentType, WPD_CONTENT_TYPE_FOLDER);

				if (!node.isDir) {
					node.size = size;
				}
				else {
					node.size = 0;
				}

				delete[] parentId;
				delete[] name;
				delete[] origName;
			}
			
			device.Reset();
			content.Reset();
			objVals.Reset();
			props.Reset();
			propsToRead.Reset();

			closeCOM();
		}

		return node;
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