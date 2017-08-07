#include <iostream>
#include <sstream>
#include <map>
#include <stack>
#include <shlwapi.h>

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

MTPObjectTree* constructTree(const map<wstring, MTPObjectTree*> & idToNodes) {
	map<wstring, vector<wstring>> idToCIds;
	MTPObjectTree* root = nullptr;
	for (auto node : idToNodes) {
		idToCIds[node.second->getId()];
		if (node.second->getParentId().length() == 0) {
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

void getDeviceContent(PWSTR rootId, IPortableDeviceContent *content, map<wstring, MTPObjectTree*> &idToObj) {
	stack<PWSTR> ids;
	ids.push(rootId);

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

bool hasChildren(const wchar_t * id)
{
	auto device = getSelectedDevice(NULL);

	if (device != nullptr) {
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

	if (device != nullptr) {
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

const wchar_t * createFolder(const wchar_t * destId, const wchar_t * path)
{
	auto device = getSelectedDevice(NULL);
	wchar_t *currId = nullptr;
	wchar_t *pathCpy = nullptr;

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

	return currId;
}

ComPtr<IPortableDeviceValues> getFileProps(const wchar_t *parentId, const wchar_t *filename, IStream *fileStream) {
	ComPtr<IPortableDeviceValues> fileProps = nullptr;

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
	return fileProps;
}

HRESULT streamCopy(IStream *source, IStream *dest, DWORD transferSize) {
	HRESULT hr = E_FAIL;

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

	return hr;
}

bool transferToDevice(const wchar_t * filepath, const wchar_t * destId, const wchar_t * destName)
{
	wstring strDestName(destName);
	size_t lastSlash = strDestName.rfind(L'/');
	wstring strDestFileName = strDestName.substr(lastSlash + 1, strDestName.length());
	wstring strDestPath = strDestName.substr(0, lastSlash);

	HRESULT hr = E_FAIL;

	const wchar_t *fullDestId = createFolder(destId, strDestPath.c_str());

	if (fullDestId != nullptr) {
		auto device = getSelectedDevice(NULL);
		if (device != nullptr) {
			ComPtr<IPortableDeviceContent> content = nullptr;
			hr = device->Content(&content);

			wstring fileExists = getObjIdByOrigName(fullDestId, strDestFileName.c_str());
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
					ComPtr<IPortableDeviceValues> fileProps = getFileProps(fullDestId, strDestFileName.c_str(), fileStream.Get());
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
				}
			}
		}
	}

	return SUCCEEDED(hr);
}

bool removeFromDevice(const wchar_t * id, const wchar_t * stopId)
{
	return false;
}
