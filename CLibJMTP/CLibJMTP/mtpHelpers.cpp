#include <iostream>
#include <sstream>
#include <map>
#include <stack>
#include <queue>
#include <algorithm>
#include <shlwapi.h>
#include <propvarutil.h>

#include "mtpHelpers.h"
#include "commonHelpers.h"

using std::cerr;
using std::endl;
using std::nothrow;
using std::ostringstream;
using std::hex;
using std::map;
using std::stack;
using std::queue;
using std::max;

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

bool supportsCommand(IPortableDevice *device, REFPROPERTYKEY command) {
	bool isSupported = false;

	if (device != nullptr) {
		ComPtr<IPortableDeviceCapabilities> caps;
		ComPtr<IPortableDeviceKeyCollection> commands;
		DWORD numCommands = 0;

		HRESULT hr = device->Capabilities(&caps);
		if (FAILED(hr)) {
			logErr("!!! Failed to get IPortableDeviceCapabilities from IPortableDevice: ", hr);
		}
		else {
			hr = caps->GetSupportedCommands(&commands);
			if (FAILED(hr)) {
				logErr("!!! Failed to get supported commands from device: ", hr);
			}
		}

		if (SUCCEEDED(hr)) {
			hr = commands->GetCount(&numCommands);
			if (FAILED(hr)) {
				logErr("!!! Failed to get the number of supported commands: ", hr);
			}
		}

		if (SUCCEEDED(hr)) {
			for (DWORD i = 0; i < numCommands && !isSupported; i++) {
				PROPERTYKEY key = WPD_PROPERTY_NULL;
				hr = commands->GetAt(i, &key);
				if (SUCCEEDED(hr)) {
					isSupported = IsEqualPropertyKey(command, key);
				}
			}
		}

	}

	return isSupported;
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

	if (close || id != nullptr) {
		device = nullptr;
	}

	auto manager = getDeviceManager();
	auto info = getClientInfo();

	if (id != nullptr && manager != nullptr && info != nullptr) {
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

MTPObjectTree* getNode(PCWSTR id, IPortableDeviceContent *content) {
	MTPObjectTree* node = new MTPObjectTree(id);

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

MTPObjectTree* constructTree(const map<wstring, MTPObjectTree*> & idToNodes, wstring rootParentId) {
	map<wstring, vector<wstring>> idToCIds;
	MTPObjectTree* root = nullptr;
	for (auto node : idToNodes) {
		idToCIds[node.second->getId()];
		if (node.second->getParentId() == rootParentId) {
			root = node.second;
		}
		else {
			idToCIds[node.second->getParentId()].push_back(node.second->getId());
		}
	}

	for (auto pNode : idToNodes) {
		MTPObjectTree *node = pNode.second;
		const vector<wstring> children = idToCIds.at(node->getId());
		for (auto cId : children) {
			MTPObjectTree* cNode = idToNodes.at(cId);
			node->children.push_back(cNode);
		}
	}

	return root;
}

MTPObjectTree* constructTree(const map<wstring, MTPObjectTree*> & idToNodes) {
	return constructTree(idToNodes, L"");
}

stack<PWSTR> getContentIDStack(wchar_t *rootId, IPortableDeviceContent *content) {
	stack<wchar_t*> tmpIds, ids;

	if (rootId != nullptr && content != nullptr) {
		ids.push(rootId);
		tmpIds.push(rootId);
	}

	while (!tmpIds.empty()) {
		PWSTR id = tmpIds.top();
		tmpIds.pop();

		ComPtr<IEnumPortableDeviceObjectIDs> objIdsEnum;
		HRESULT hr = content->EnumObjects(0, id, nullptr, &objIdsEnum);

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
					tmpIds.push(objIds[i]);
					ids.push(objIds[i]);
				}
			}
		}
	}


	return ids;
}

void getDeviceContent(PWSTR rootId, IPortableDeviceContent *content, map<wstring, MTPObjectTree*> &idToObj) {
	stack<PWSTR> ids;
	
	if (rootId != nullptr && content != nullptr) {
		ids.push(rootId);
	}

	while (!ids.empty()) {
		PWSTR id = ids.top();
		ids.pop();

		ComPtr<IEnumPortableDeviceObjectIDs> objIdsEnum;
		HRESULT hr = content->EnumObjects(0, id, nullptr, &objIdsEnum);

		MTPObjectTree* oTree = getNode(id, content);
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
					ids.push(objIds[i]);
				}
			}
		}

		if (wcscmp(id, rootId) != 0) {
			CoTaskMemFree(id);
			id = nullptr;
		}
	}
}

MTPObjectTree* getDeviceContent()
{
	return getDeviceContent(WPD_DEVICE_OBJECT_ID);
}

MTPObjectTree * getDeviceContent(const wchar_t * rootId)
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
			wchar_t *rootIdCpy = nullptr;
			wcsAllocCpy(&rootIdCpy, rootId);
			getDeviceContent(rootIdCpy, content.Get(), oTreeNodes);
			delete[] rootIdCpy;

			oTree = constructTree(oTreeNodes, oTreeNodes[rootId]->getParentId().c_str());
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

	if (deviceId != nullptr) {
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
	}

	return description;
}

PWSTR getDeviceFriendlyName(PWSTR deviceId) {
	DWORD fNameLength = 0;
	PWSTR friendlyName = nullptr;

	if (deviceId != nullptr) {
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
	}

	return friendlyName;
}

PWSTR getDeviceManufacturer(PWSTR deviceId) {
	DWORD manuLength = 0;
	PWSTR manufacturer = nullptr;
	
	if (deviceId != nullptr) {
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

bool hasChildren(const wchar_t * id)
{
	auto device = getSelectedDevice(NULL);

	if (device != nullptr && id != nullptr) {
		ComPtr<IPortableDeviceContent> content = nullptr;
		HRESULT hr = device->Content(&content);

		if (FAILED(hr)) {
			logErr("!!! Failed to get IPortableDeviceContent: ", hr);
		}
		else {
			ComPtr<IEnumPortableDeviceObjectIDs> objIdsEnum;
			HRESULT hr = content->EnumObjects(0, id, nullptr, &objIdsEnum);

			if (FAILED(hr)) {
				logErr("!!! Failed to get IEnumPortableDeviceObjectIDs: ", hr);
			}

			if (SUCCEEDED(hr)) {
				DWORD fetched = 0;
				PWSTR objIds[1] = { nullptr };
				hr = objIdsEnum->Next(1,
					objIds,
					&fetched);

				if (fetched > 0) {
					for (unsigned int i = 0; i < fetched; i++) {
						CoTaskMemFree(objIds[i]);
						objIds[i] = nullptr;
					}
					return true;
				}
			}
		}
	}

	return false;
}

wstring getObjIdByOrigName(const wchar_t * parentId, const wchar_t * origName)
{
	auto device = getSelectedDevice(NULL);
	wstring objId = L"";

	if (device != nullptr && parentId != nullptr && origName != nullptr) {
		ComPtr<IPortableDeviceContent> content = nullptr;
		HRESULT hr = device->Content(&content);

		if (FAILED(hr)) {
			logErr("!!! Failed to get IPortableDeviceContent: ", hr);
		}
		else {
			ComPtr<IEnumPortableDeviceObjectIDs> objIdsEnum;
			HRESULT hr = content->EnumObjects(0, parentId, nullptr, &objIdsEnum);

			if (FAILED(hr)) {
				logErr("!!! Failed to get IEnumPortableDeviceObjectIDs: ", hr);
			}

			while (hr == S_OK && objId == L"") {
				DWORD fetched = 0;
				PWSTR objIds[NUM_OBJECTS_TO_REQUEST] = { nullptr };
				hr = objIdsEnum->Next(NUM_OBJECTS_TO_REQUEST,
					objIds,
					&fetched);

				if (SUCCEEDED(hr)) {
					for (DWORD i = 0; i < fetched; i++) {
						MTPObjectTree *node = getNode(objIds[i], content.Get());
						if (node->getOrigName().compare(origName) == 0) {
							objId = node->getId();
							break;
						}

						CoTaskMemFree(objIds[i]);
						objIds[i] = nullptr;
					}
				}
			}
		}
	}

	return objId;
}

ComPtr<IPortableDeviceValues> getFolderProps(const wchar_t *parentId, const wchar_t *folderName) {
	ComPtr<IPortableDeviceValues> folderProps = nullptr;

	HRESULT hr = CoCreateInstance(CLSID_PortableDeviceValues,
		nullptr,
		CLSCTX_INPROC_SERVER,
		IID_PPV_ARGS(&folderProps));

	if (SUCCEEDED(hr)) {
		hr = folderProps->SetStringValue(WPD_OBJECT_PARENT_ID, parentId);
	}
	if (SUCCEEDED(hr)) {
		hr = folderProps->SetStringValue(WPD_OBJECT_NAME, folderName);
	}
	if (SUCCEEDED(hr)) {
		hr = folderProps->SetGuidValue(WPD_OBJECT_CONTENT_TYPE, WPD_CONTENT_TYPE_FOLDER);
	}

	if (SUCCEEDED(hr)) {
		return folderProps;
	}
	else {
		logErr("!!! Failed to retrieve folder properties: ", hr);
		return nullptr;
	}
}

pair<wstring, wstring> createFolder(const wchar_t * destId, const wchar_t * path)
{
	auto device = getSelectedDevice(NULL);
	wchar_t *currId = nullptr;
	wchar_t *pathCpy = nullptr;

	// first is the ID of the first folder created or empty if all folders exist
	// second is the ID of the last folder in the path
	pair<wstring, wstring> retPair;
	retPair.first = L"";
	retPair.second = L"";

	if (destId != nullptr && path != nullptr) {
		wcsAllocCpy(&currId, destId);
		wcsAllocCpy(&pathCpy, path);

		if (device != nullptr) {
			ComPtr<IPortableDeviceValues> objProps;
			ComPtr<IPortableDeviceContent> content;

			HRESULT hr = device->Content(&content);

			wchar_t *buffer;
			const wchar_t *subPath = wcstok_s(pathCpy, L"/", &buffer);
			while (subPath != NULL) {
				wstring existingId = getObjIdByOrigName(currId, subPath);
				PWSTR folderId = nullptr;

				if (existingId == L"") {
					objProps = getFolderProps(currId, subPath);
					if (objProps == nullptr) {
						currId = nullptr;
						break;
					}

					hr = content->CreateObjectWithPropertiesOnly(objProps.Get(), &folderId);
					if (FAILED(hr)) {
						logErr("!!! Failed to create folder: ", hr);
						currId = nullptr;
						break;
					}
					else if (retPair.first.empty()) {
						retPair.first.assign(folderId);
					}

					wcsAllocCpy(&currId, folderId);
					CoTaskMemFree(folderId);
					folderId = nullptr;
				}
				else {
					wcsAllocCpy(&currId, existingId.c_str());
				}

				subPath = wcstok_s(NULL, L"/", &buffer);
			}
		}
		delete[] pathCpy;
	}

	if (currId != nullptr) {
		retPair.second.assign(currId);
	}
	delete[] currId;

	return retPair;
}

ComPtr<IPortableDeviceValues> getFileProps(const wchar_t *parentId, const wchar_t *filename, IStream *fileStream) {
	ComPtr<IPortableDeviceValues> fileProps = nullptr;

	if (filename != nullptr && fileStream != nullptr && parentId != nullptr) {
		wstring strFile(filename);
		size_t dotIndex = strFile.rfind(L".");

		HRESULT hr = CoCreateInstance(CLSID_PortableDeviceValues,
			nullptr,
			CLSCTX_INPROC_SERVER,
			IID_PPV_ARGS(&fileProps));

		if (FAILED(hr) && fileProps != nullptr) {
			logErr("!!! Failed to CoCreateInstance CLSID_PortableDeviceValues: ", hr);
		}
		else {
			// Parent ID
			hr = fileProps->SetStringValue(WPD_OBJECT_PARENT_ID, parentId);
			if (FAILED(hr)) {
				logErr("!!! Failed to set WPD_OBJECT_PARENT_ID: ", hr);
			}
			else {
				// File Size
				STATSTG statstg = { 0 };
				hr = fileStream->Stat(&statstg, STATFLAG_NONAME);
				if (FAILED(hr)) {
					logErr("!!! Failed to get file's total size: ", hr);
				}
				else {
					hr = fileProps->SetUnsignedLargeIntegerValue(WPD_OBJECT_SIZE, statstg.cbSize.QuadPart);
					if (FAILED(hr)) {
						logErr("!!! Failed to set WPD_OBJECT_SIZE: ", hr);
					}
				}

				if (SUCCEEDED(hr)) {
					// Original file name
					hr = fileProps->SetStringValue(WPD_OBJECT_ORIGINAL_FILE_NAME, filename);
					if (FAILED(hr)) {
						logErr("!!! Failed to set WPD_OBJECT_ORIGINAL_FILE_NAME: ", hr);
					}
				}

				if (SUCCEEDED(hr)) {
					// File name
					wstring strName;

					if (dotIndex == wstring::npos) {
						strName = strFile;
					}
					else {
						strName = strFile.substr(0, dotIndex - 1);
					}

					hr = fileProps->SetStringValue(WPD_OBJECT_NAME, strName.c_str());
					if (FAILED(hr)) {
						logErr("!!! Failed to set WPD_OBJECT_NAME: ", hr);
					}
				}

				if (SUCCEEDED(hr)) {
					// Content type
					hr = fileProps->SetGuidValue(WPD_OBJECT_CONTENT_TYPE, WPD_CONTENT_TYPE_AUDIO);
					if (FAILED(hr)) {
						logErr("!!! Failed to set WPD_OBJECT_CONTENT_TYPE to WPD_CONTENT_TYPE_AUDIO: ", hr);
					}
					else if (dotIndex == wstring::npos) {
						hr = E_FAIL;
						logErr("!!! File has no extension: ", hr);
					}
					else {
						GUID format;
						wstring extension = strFile.substr(dotIndex, strFile.length());

						if (_wcsicmp(extension.c_str(), L"flac") == 0) {
							format = WPD_OBJECT_FORMAT_FLAC;
						}
						else if (_wcsicmp(extension.c_str(), L"mp3") == 0) {
							format = WPD_OBJECT_FORMAT_MP3;
						}
						else if (_wcsicmp(extension.c_str(), L"wma") == 0) {
							format = WPD_OBJECT_FORMAT_WMA;
						}
						else if (_wcsicmp(extension.c_str(), L"m4a") == 0) {
							format = WPD_OBJECT_FORMAT_M4A;
						}
						else {
							format = WPD_OBJECT_FORMAT_UNSPECIFIED;
						}

						hr = fileProps->SetGuidValue(WPD_OBJECT_FORMAT, format);
						if (FAILED(hr)) {
							logErr("!!! Failed to set WPD_OBJECT_FORMAT: ", hr);
						}
					}
				}
			}
		}

		if (FAILED(hr)) {
			fileProps = nullptr;
		}
	}

	return fileProps;
}

HRESULT streamCopy(IStream *source, IStream *dest, DWORD transferSize) {
	HRESULT hr = E_FAIL;

	if (source != nullptr && dest != nullptr) {
		BYTE *objData = new (nothrow) BYTE[transferSize];
		if (objData == nullptr) {
			logErr("!!! Failed to allocate transfer buffer: ", hr);
		}
		else {
			DWORD bytesRead = 0;
			DWORD bytesWritten = 0;

			do {
				hr = source->Read(objData, transferSize, &bytesRead);
				if (FAILED(hr)) {
					logErr("!!! Failed to read from source stream: ", hr);
				}
				else {
					hr = dest->Write(objData, bytesRead, &bytesWritten);
					if (FAILED(hr)) {
						logErr("!!! Failed to write to destination stream: ", hr);
					}
				}
			} while (SUCCEEDED(hr) && bytesRead > 0);
		}

		delete[] objData;
		objData = nullptr;
	}

	return hr;
}

MTPObjectTree* transferToDevice(const wchar_t * filepath, const wchar_t * destId, const wchar_t * destName)
{
	MTPObjectTree *newSubTree = nullptr;
	wstring newIdStr;

	if (filepath != nullptr && destId != nullptr && destName != nullptr) {
		wstring strDestName(destName);
		size_t lastSlash = strDestName.rfind(L'/');
		wstring strDestFileName = strDestName.substr(lastSlash + 1, strDestName.length());
		wstring strDestPath = strDestName.substr(0, lastSlash);

		HRESULT hr = E_FAIL;

		auto folderPair = createFolder(destId, strDestPath.c_str());
		wstring fullDestId = folderPair.second;

		if (!fullDestId.empty()) {
			auto device = getSelectedDevice(NULL);
			if (device != nullptr) {
				ComPtr<IPortableDeviceContent> content = nullptr;
				hr = device->Content(&content);

				wstring fileExists = getObjIdByOrigName(fullDestId.c_str(), strDestFileName.c_str());
				if (fileExists.length() != 0) {
					std::wcerr << L"!!! File already exists: " << strDestFileName << endl;
				}
				else {
					ComPtr<IStream> fileStream;
					hr = SHCreateStreamOnFileEx(filepath, STGM_READ, FILE_ATTRIBUTE_NORMAL, FALSE, nullptr, &fileStream);
					if (FAILED(hr)) {
						logErr("!!! Failed to open file for transfer: ", hr);
					}
					else {
						ComPtr<IPortableDeviceValues> fileProps = getFileProps(fullDestId.c_str(), strDestFileName.c_str(), fileStream.Get());
						ComPtr<IStream> tmpStream;
						ComPtr<IPortableDeviceDataStream> objStream;
						DWORD optimalTransferSizeBytes = 0;

						if (fileProps != nullptr) {
							hr = content->CreateObjectWithPropertiesAndData(fileProps.Get(),
								&tmpStream,
								&optimalTransferSizeBytes,
								nullptr);

							if (SUCCEEDED(hr)) {
								hr = tmpStream.As(&objStream);
								if (FAILED(hr)) {
									logErr("!!! Failed to QueryInterface for IPortableDeviceDataStream: ", hr);
								}
							}

							if (SUCCEEDED(hr)) {
								DWORD bytesWritten = 0;
								hr = streamCopy(fileStream.Get(), objStream.Get(), optimalTransferSizeBytes);
								if (FAILED(hr)) {
									logErr("!!! Failed to transfer object to device: ", hr);
								}
							}
						}

						if (SUCCEEDED(hr)) {
							hr = objStream->Commit(STGC_DEFAULT);
							if (FAILED(hr)) {
								logErr("!!! Failed to commit object to device: ", hr);
							}
						}

						if (SUCCEEDED(hr)) {
							PWSTR newId = nullptr;
							hr = objStream->GetObjectID(&newId);
							if (FAILED(hr)) {
								logErr("!!! Failed to get newly transferred object's ID: ", hr);
							}
							else {
								newIdStr.assign(newId);
							}
							CoTaskMemFree(newId);
							newId = nullptr;
						}
					}
				}
			}
		}

		if (!newIdStr.empty()) {
			wstring subRoot;
			if (folderPair.first.empty()) {
				subRoot = newIdStr;
			}
			else {
				subRoot = folderPair.first;
			}
			newSubTree = getDeviceContent(subRoot.c_str());
		}
	}

	return newSubTree;
}

HRESULT removeFromDevice(const wchar_t * id) {
	auto device = getSelectedDevice(NULL);
	HRESULT hr = E_FAIL;

	if (device != nullptr && id != nullptr) {
		ComPtr<IPortableDeviceContent> content;
		hr = device->Content(&content);
		if (FAILED(hr)) {
			logErr("!!! Failed to get IPortableDeviceContent: ", hr);
		}
		else {
			ComPtr<IPortableDevicePropVariantCollection> objsToDelete;

			hr = CoCreateInstance(CLSID_PortableDevicePropVariantCollection,
				nullptr,
				CLSCTX_INPROC_SERVER,
				IID_PPV_ARGS(&objsToDelete));

			if (FAILED(hr)) {
				logErr("!!! Failed to CoCreateInstance CLSID_PortableDevicePropVariantCollection: ", hr);
			}
			else {
				PROPVARIANT pv = { 0 };
				hr = InitPropVariantFromString(id, &pv);
				if (FAILED(hr)) {
					logErr("!!! Failed to InitPropVariantFromString: ", hr);
				}
				else {
					hr = objsToDelete->Add(&pv);
					if (FAILED(hr)) {
						logErr("!!! Failed to add object to IPortableDevicePropVariantCollection: ", hr);
					}
					else {
						hr = content->Delete(PORTABLE_DEVICE_DELETE_NO_RECURSION,
							objsToDelete.Get(),
							nullptr);

						if (hr != S_OK) {
							logErr("!!! Failed to delete object: ", hr);
						}
					}
				}

				PropVariantClear(&pv);
			}
		}
	}

	return hr;
}

wstring removeFromDevice(const wchar_t * id, const wchar_t * stopId)
{
	auto device = getSelectedDevice(NULL);
	ComPtr<IPortableDeviceContent> content = nullptr;
	wchar_t *idCpy = nullptr;
	HRESULT hr = E_FAIL;
	wstring highestDeleted;

	if (device != nullptr && id != nullptr) {
		wcsAllocCpy(&idCpy, id);
		hr = device->Content(&content);
		if (FAILED(hr)) {
			logErr("!!! Failed to get IPortableDeviceContent: ", hr);
		}
		else {
			queue<wstring> parentIdsToDelete;	// Queue for parent objects to delete

			if (stopId != nullptr) {
				MTPObjectTree *node = getNode(idCpy, content.Get());
				bool stopFound = false;
				while (!stopFound && !node->getParentId().empty()) {
					if (node->getParentId().compare(stopId) == 0) {
						stopFound = true;
					}
					else {
						parentIdsToDelete.push(node->getParentId());
					}

					MTPObjectTree *tmpNode = getNode(node->getParentId().c_str(), content.Get());
					delete node;
					node = tmpNode;
				}
				delete node;

				if (!stopFound) {
					logErr("!!! Failed to find stopId.  No parent objects will be deleted: ", S_FALSE);
					queue<wstring> empty;
					std::swap(parentIdsToDelete, empty);
				}
			}

			stack<PWSTR> idsToDelete = getContentIDStack(idCpy, content.Get());	// stack of children objects to delete

			while (!idsToDelete.empty() && hr == S_OK) {
				PWSTR tmpId = idsToDelete.top();
				idsToDelete.pop();

				hr = removeFromDevice(tmpId);
				if (hr == S_OK) {
					highestDeleted.assign(tmpId);
				}

				if (wcscmp(tmpId, id) != 0) {
					CoTaskMemFree(tmpId);
					tmpId = nullptr;
				}
			}

			while (!parentIdsToDelete.empty() && hr == S_OK) {
				wstring tmpId = parentIdsToDelete.front();
				parentIdsToDelete.pop();
				if (!hasChildren(tmpId.c_str())) {
					hr = removeFromDevice(tmpId.c_str());
					if (hr == S_OK) {
						highestDeleted.assign(tmpId);
					}
				}
				else {
					break;
				}
			}
		}
	}

	delete[] idCpy;
	return highestDeleted;
}

bool transferFromDevice(const wchar_t * id, const wchar_t * destFilepath)
{
	auto device = getSelectedDevice(NULL);
	HRESULT hr = E_FAIL;

	if (device != nullptr && id != nullptr && destFilepath != nullptr) {
		ComPtr<IPortableDeviceContent> content;
		hr = device->Content(&content);
		if (FAILED(hr)) {
			logErr("!!! Failed to get IPortableDeviceContent: ", hr);
		}
		else {
			ComPtr<IPortableDeviceResources> resources;
			ComPtr<IStream> objStream;
			ComPtr<IStream> fileStream;

			DWORD optimalTransferSizeBytes = 0;

			hr = content->Transfer(&resources);
			if (FAILED(hr)) {
				logErr("!!! Failed to get IPortableDeviceResources from IPortableDeviceContent: ", hr);
			}

			if (SUCCEEDED(hr)) {
				hr = resources->GetStream(id,
					WPD_RESOURCE_DEFAULT,
					STGM_READ,
					&optimalTransferSizeBytes,
					&objStream);
				if (FAILED(hr)) {
					logErr("!!! Failed to get IStream for the object data", hr);
				}
			}

			// create intermediate folders
			if (SUCCEEDED(hr)) {
				wchar_t *pathCpy = nullptr;
				wcsAllocCpy(&pathCpy, destFilepath);

				for (unsigned int i = 0; i < wcslen(pathCpy); i++) {
					if (pathCpy[i] == L'\0') {
						break;
					}
					else if (pathCpy[i] == L'\\' || pathCpy[i] == L'/') {
						pathCpy[i] = L'\0';
						if (!CreateDirectoryW(pathCpy, NULL)) {
							DWORD lastError = GetLastError();
							if (lastError != ERROR_ALREADY_EXISTS) {
								hr = HRESULT_FROM_WIN32(lastError);
								logErr("!!! Failed to create intermediate directory: ", hr);
								break;
							}
						}
						pathCpy[i] = L'\\';
					}
				}

				delete[] pathCpy;
			}

			if (SUCCEEDED(hr)) {
				hr = SHCreateStreamOnFileEx(destFilepath,
					STGM_CREATE | STGM_WRITE,
					FILE_ATTRIBUTE_NORMAL,
					FALSE,
					nullptr,
					&fileStream);
				if (FAILED(hr)) {
					logErr("!!! Failed to create temporary file to transfer object: ", hr);
				}
			}

			if (SUCCEEDED(hr)) {
				hr = streamCopy(objStream.Get(), fileStream.Get(), optimalTransferSizeBytes);
				if (FAILED(hr)) {
					logErr("!!! Failed to transfer object from device: ", hr);
				}
			}
		}
	}

	return hr == S_OK;
}

bool moveOnDevice(const wchar_t * id, const wchar_t * destFolderId)
{
	auto device = getSelectedDevice(NULL);
	HRESULT hr = E_FAIL;

	if (device != nullptr && id != nullptr && destFolderId != nullptr) {
		if (!supportsCommand(device.Get(), WPD_COMMAND_OBJECT_MANAGEMENT_MOVE_OBJECTS)) {
			logErr("!!! This device does not support the move operation: ", hr);
			return false;
		}

		ComPtr<IPortableDeviceContent> content;
		ComPtr<IPortableDevicePropVariantCollection> objsToMove;
		
		hr = device->Content(&content);
		if (FAILED(hr)) {
			logErr("!!! Failed to get IPortableDeviceContent: ", hr);
		}
		else {
			hr = CoCreateInstance(CLSID_PortableDevicePropVariantCollection,
				nullptr,
				CLSCTX_INPROC_SERVER,
				IID_PPV_ARGS(&objsToMove));
			if (FAILED(hr)) {
				logErr("!!! Failed to CoCreateInstance CLSID_PortableDevicePropVariantCollection: ", hr);
			}
			else {
				PROPVARIANT pv = { 0 };
				hr = InitPropVariantFromString(id, &pv);
				if (FAILED(hr)) {
					logErr("!!! Failed to move an object on the device because we could no allocate memory for the object identifier string: ", hr);
				}
				else {
					hr = objsToMove->Add(&pv);
					if (FAILED(hr)) {
						logErr("!!! Failed to add object to IPortableDevicePropVariantCollection: ", hr);
					}
					else {
						hr = content->Move(objsToMove.Get(),
							destFolderId,
							nullptr);
						if (hr != S_OK) {
							logErr("!!! Failed to move object: ", hr);
						}
					}
				}
			}
		}
	}

	return hr == S_OK;
}
