#include <iostream>
#include <sstream>
#include <map>
#include <stack>

#include "mtpHelpers.h"
#include "commonHelpers.h"

using std::cerr;
using std::endl;
using std::nothrow;
using std::ostringstream;
using std::hex;
using std::map;
using std::stack;

ComPtr<IPortableDeviceManager> getDeviceManager(bool close);
ComPtr<IPortableDevice> getSelectedDevice(const wchar_t* id, bool close);

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
	getDeviceManager(true);
	getSelectedDevice(nullptr, true);
	CoUninitialize();
	//cout << "COM closed" << endl;
}

ComPtr<IPortableDeviceManager> getDeviceManager(bool close) {

	static ComPtr<IPortableDeviceManager> deviceManager = nullptr;

	if (close) {
		deviceManager = nullptr;
	}
	else if (deviceManager == nullptr) {
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

ComPtr<IPortableDeviceManager> getDeviceManager() {
	return getDeviceManager(false);
}

ComPtr<IPortableDeviceValues> getClientInfo()
{
	ComPtr<IPortableDeviceValues> info = nullptr;

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

	return info;
}

ComPtr<IPortableDevice> getSelectedDevice(const wchar_t* id, bool close)
{
	static ComPtr<IPortableDevice> device = nullptr;
	
	if (close) {
		device = nullptr;
	}
	else {
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
	}
	
	return device;
}

ComPtr<IPortableDevice> getSelectedDevice(const wchar_t* id) {
	return getSelectedDevice(id, false);
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

MTPObjectTree* getNode(PWSTR id, IPortableDeviceContent *content) {
	MTPObjectTree* node = new MTPObjectTree(id);

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

				tmpHr = propsToRead->Add(WPD_OBJECT_PERSISTENT_UNIQUE_ID);
				if (FAILED(tmpHr)) {
					logErr("!!! Failed to add WPD_OBJECT_PERSISTENT_UNIQUE_ID to IPortableDeviceKeyCollection: ", tmpHr);
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

				tmpHr = propsToRead->Add(WPD_STORAGE_CAPACITY);
				if (FAILED(tmpHr)) {
					logErr("!!! Failed to add WPD_STORAGE_CAPACITY to IPortableDeviceKeyCollection: ", tmpHr);
				}

				hr = props->GetValues(id,
					propsToRead.Get(),
					&objVals);

				if (FAILED(hr)) {
					logErr("!!! Failed to get all properties for object: ", hr);
				}
				else {
					PWSTR parentId = nullptr;
					PWSTR persistId = nullptr;
					PWSTR name = nullptr;
					PWSTR origName = nullptr;
					ULONGLONG size = 0;
					ULONGLONG cap = 0;

					getStringProperty(objVals.Get(), WPD_OBJECT_PARENT_ID, &parentId);
					getStringProperty(objVals.Get(), WPD_OBJECT_PERSISTENT_UNIQUE_ID, &persistId);
					getStringProperty(objVals.Get(), WPD_OBJECT_NAME, &name);
					getStringProperty(objVals.Get(), WPD_OBJECT_ORIGINAL_FILE_NAME, &origName);
					getULongLongProperty(objVals.Get(), WPD_OBJECT_SIZE, &size);
					getULongLongProperty(objVals.Get(), WPD_STORAGE_CAPACITY, &cap);

					node->setParentId(parentId);
					node->setPersistId(persistId);
					node->setName(name);
					node->setOrigName(origName);
					node->setSize(size);
					node->setCapacity(cap);
					delete[] parentId;
					delete[] persistId;
					delete[] name;
					delete[] origName;
				}
			}
		}
	}

	return node;
}

void constructTree(MTPObjectTree* root, const map<wstring, vector<wstring>> & idToCIds, const map<wstring, MTPObjectTree*> & idToNodes) {
	const vector<wstring> children = idToCIds.at(root->getId());
	for (auto cId : children) {
		MTPObjectTree* cNode = idToNodes.at(cId);
		constructTree(cNode, idToCIds, idToNodes);
		root->children.push_back(cNode);
	}
}

MTPObjectTree* constructTree(const map<wstring, MTPObjectTree*> & idToNodes) {
	map<wstring, vector<wstring>> idToCIds;
	MTPObjectTree* root = nullptr;
	for (auto node : idToNodes) {
		idToCIds[node.second->getId()];
		if (node.second->getParentId().length() == 0) {
			root = node.second;
		}
	}
	for (auto node : idToNodes) {
		idToCIds[node.second->getParentId()].push_back(node.second->getId());
	}
	constructTree(root, idToCIds, idToNodes);

	return root;
}

void getDeviceContent(PWSTR rootId, IPortableDeviceContent *content, map<wstring, MTPObjectTree*> &idToObj) {
	ComPtr<IEnumPortableDeviceObjectIDs> objIdsEnum;
	HRESULT hr = content->EnumObjects(0, rootId, nullptr, &objIdsEnum);

	MTPObjectTree* oTree = getNode(rootId, content);
	idToObj[oTree->getId()] = oTree;

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
				getDeviceContent(objIds[i], content, idToObj);
			}
		}
	}
}

MTPObjectTree* getDeviceContent()
{
	auto device = getSelectedDevice(NULL);
	map<wstring, MTPObjectTree*> oTreeNodes;
	MTPObjectTree* oTree = nullptr;
	ComPtr<IPortableDeviceContent> content = nullptr;

	if (device != nullptr) {
		HRESULT hr = device->Content(&content);
		if (FAILED(hr)) {
			logErr("!!! Failed to get IPortableDeviceContent: ", hr);
		}
		else {
			getDeviceContent(WPD_DEVICE_OBJECT_ID, content.Get(), oTreeNodes);
			oTree = constructTree(oTreeNodes);
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
	cerr << msg << formatHR(hr) << std::endl;
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
