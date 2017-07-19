#include <iostream>
#include <sstream>
#include <map>

#include "mtpHelpers.h"

using std::cerr;
using std::endl;
using std::nothrow;
using std::ostringstream;
using std::hex;
using std::map;

HRESULT initCOM() {
	static HRESULT hr = E_FAIL;

	if (FAILED(hr)) {
		hr = CoInitializeEx(nullptr, COINIT_MULTITHREADED);

		if (FAILED(hr)) {
			logErr("!!! Failed to CoInitialize: ", hr);
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
			logErr("!!! Failed to get CoCreateInstance: ", hr);
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
			logErr("!!! Failed to CoCreateInstance CLSID_PortableDeviceValues: ", hr);
		}
		else {
			hr = info->SetUnsignedIntegerValue(WPD_CLIENT_SECURITY_QUALITY_OF_SERVICE, SECURITY_IMPERSONATION);
			if (FAILED(hr))
			{
				info = nullptr;
				logErr("!!! Failed to set WPD_CLIENT_SECURITY_QUALITY_OF_SERVICE: ", hr);
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
			logErr("!!! Failed to CoCreateInstance CLSID_PortableDeviceFTM: ", hr);
		}
		else {
			hr = device->Open(id, info.Get());
			if (FAILED(hr)) {
				device = nullptr;
				logErr("!!! Failed to open device: ", hr);
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
			logErr("!!! Unable to get device count: ", hr);
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
					logErr("!!! Failed to retrieve device IDs: ", hr);
				}

				delete[] deviceIds;
				deviceIds = nullptr;
			}
		}
	}

	return devList;
}

MTPObjectTree getNode(PWSTR id, IPortableDeviceContent *content) {
	MTPObjectTree node;
	node.setId(id);

	//auto device = getSelectedDevice(nullptr);

	if (content != nullptr && id != nullptr) {
		ComPtr<IPortableDeviceValues> objVals;
		ComPtr<IPortableDeviceProperties> props;
		ComPtr<IPortableDeviceKeyCollection> propsToRead;

		HRESULT hr = content->Properties(&props);
		if (FAILED(hr)) {
			logErr("!!! Failed to get IPortableDeviceProperties: ", hr);
		}
		else {
			hr = CoCreateInstance(CLSID_PortableDeviceKeyCollection,
				nullptr,
				CLSCTX_INPROC_SERVER,
				IID_PPV_ARGS(&propsToRead));

			if (FAILED(hr)) {
				logErr("!!! Failed to CoCreateInstance CLSID_PortableDeviceKeyCollection: ", hr);
			}
			else {
				HRESULT tmpHr = propsToRead->Add(WPD_OBJECT_PARENT_ID);
				if (FAILED(tmpHr)) {
					logErr("!!! Failed to add WPD_OBJECT_PARENT_ID to IPortableDeviceKeyCollection: ", tmpHr);
				}

				tmpHr = propsToRead->Add(WPD_OBJECT_NAME);
				if (FAILED(tmpHr)) {
					logErr("!!! Failed to add WPD_OBJECT_NAME to IPortableDeviceKeyCollection: ", tmpHr);
				}

				tmpHr = propsToRead->Add(WPD_OBJECT_ORIGINAL_FILE_NAME);
				if (FAILED(tmpHr)) {
					logErr("!!! Failed to add WPD_OBJECT_ORIGINAL_FILE_NAME to IPortableDeviceKeyCollection: ", tmpHr);
				}

				tmpHr = propsToRead->Add(WPD_OBJECT_SIZE);
				if (FAILED(tmpHr)) {
					logErr("!!! Failed to add WPD_OBJECT_SIZE to IPortableDeviceKeyCollection: ", tmpHr);
				}

				hr = props->GetValues(id,
					propsToRead.Get(),
					&objVals);

				if (FAILED(hr)) {
					logErr("!!! Failed to get all properties for object: ", hr);
				}
				else {
					PWSTR parentId = nullptr;
					PWSTR name = nullptr;
					PWSTR origName = nullptr;
					ULONGLONG size = 0;

					getStringProperty(objVals.Get(), WPD_OBJECT_PARENT_ID, &parentId);
					getStringProperty(objVals.Get(), WPD_OBJECT_NAME, &name);
					getStringProperty(objVals.Get(), WPD_OBJECT_ORIGINAL_FILE_NAME, &origName);
					getULongLongProperty(objVals.Get(), WPD_OBJECT_SIZE, &size);

					node.setParentId(parentId);
					node.setName(name);
					node.setOrigName(origName);
					node.setSize(size);
					delete[] parentId;
					delete[] name;
					delete[] origName;
				}
			}
		}
	}

	return node;
}

MTPObjectTree constructTree(const vector<MTPObjectTree> & nodes) {
	return MTPObjectTree();
}

void getDeviceContent(PWSTR rootId, IPortableDeviceContent *content, vector<MTPObjectTree> &mtpObjs) {
	ComPtr<IEnumPortableDeviceObjectIDs> objIdsEnum;
	HRESULT hr = content->EnumObjects(0, rootId, nullptr, &objIdsEnum);

	MTPObjectTree oTree = getNode(rootId, content);
	std::wcout << oTree.toString() << endl;
	mtpObjs.push_back(oTree);

	if (FAILED(hr)) {
		logErr("!!! Failed to get IEnumPortableDeviceObjectIDs: ", hr);
	}

	while (hr == S_OK) {
		DWORD fetched = 0;
		PWSTR objIds[NUM_OBJECTS_TO_REQUEST] = { nullptr };
		hr = objIdsEnum->Next(NUM_OBJECTS_TO_REQUEST,
			objIds,
			&fetched);

		if (SUCCEEDED(hr)) {
			for (DWORD i = 0; i < fetched; i++) {
				getDeviceContent(objIds[i], content, mtpObjs);
			}
		}
	}
}

MTPObjectTree getDeviceContent()
{
	auto device = getSelectedDevice(NULL);
	vector<MTPObjectTree> oTreeNodes;
	MTPObjectTree oTree;
	ComPtr<IPortableDeviceContent> content = nullptr;

	if (device != nullptr) {
		HRESULT hr = device->Content(&content);
		if (FAILED(hr)) {
			logErr("!!! Failed to get IPortableDeviceContent: ", hr);
		}
		else {
			getDeviceContent(WPD_DEVICE_OBJECT_ID, content.Get(), oTreeNodes);
		}
	}

	return oTree;
}

string formatHR(HRESULT hr) {
	ostringstream strStream;

	strStream << "hr=0x" << hex << hr;
	return strStream.str();
}

void logErr(char * msg, HRESULT hr)
{
	logErr(msg, hr);
}

PWSTR getDeviceDescription(PWSTR deviceId) {
	DWORD descLength = 0;
	PWSTR description = nullptr;
	auto deviceManager = getDeviceManager();

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

	return description;
}

PWSTR getDeviceFriendlyName(PWSTR deviceId) {
	DWORD fNameLength = 0;
	PWSTR friendlyName = nullptr;
	auto deviceManager = getDeviceManager();

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

	return friendlyName;
}

PWSTR getDeviceManufacturer(PWSTR deviceId) {
	DWORD manuLength = 0;
	PWSTR manufacturer = nullptr;
	auto deviceManager = getDeviceManager();

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

	return manufacturer;
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
