#pragma once
#include <PortableDeviceApi.h>
#include <PortableDevice.h>
#include <wrl/client.h>
#include <string>
#include <vector>

#include "mtpModels.h"

#define NUM_OBJECTS_TO_REQUEST 100

using std::string;
using std::vector;
using std::pair;
using Microsoft::WRL::ComPtr;

HRESULT initCOM();
void closeCOM();
ComPtr<IPortableDeviceManager> getDeviceManager();
ComPtr<IPortableDeviceValues> getClientInfo();
ComPtr<IPortableDevice> getSelectedDevice(const wchar_t* id);
vector<MTPDevice> getDevices();
MTPObjectTree* getDeviceContent();
MTPObjectTree* getDeviceContent(const wchar_t* rootId);

string formatHR(HRESULT hr);
void logErr(char *msg, HRESULT hr);
PWSTR getDeviceDescription(PWSTR deviceId);
PWSTR getDeviceFriendlyName(PWSTR deviceId);
PWSTR getDeviceManufacturer(PWSTR deviceId);

void getStringProperty(IPortableDeviceValues *values, REFPROPERTYKEY key, PWSTR *destination);void getStringProperty(IPortableDeviceValues *values, REFPROPERTYKEY key, PWSTR *destination);
void getULongLongProperty(IPortableDeviceValues *values, REFPROPERTYKEY key, ULONGLONG *destination); void getStringProperty(IPortableDeviceValues *values, REFPROPERTYKEY key, ULONGLONG *destination);

bool hasChildren(const wchar_t *id);
wstring getObjIdByOrigName(const wchar_t *parentId, const wchar_t *origName);
pair<wstring, wstring> createFolder(const wchar_t *destId, const wchar_t *path);
MTPObjectTree* transferToDevice(const wchar_t *filepath, const wchar_t *destId, const wchar_t *destName);
wstring removeFromDevice(const wchar_t *id, const wchar_t *stopId);
bool transferFromDevice(const wchar_t *id, const wchar_t *destFilepath);
bool moveOnDevice(const wchar_t *id, const wchar_t *destFolderId);