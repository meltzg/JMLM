#pragma once
#include <PortableDeviceApi.h>
#include <PortableDevice.h>
#include <wrl/client.h>
#include <string>
#include <vector>

#include "mtpModels.h"

using std::string;
using std::vector;
using Microsoft::WRL::ComPtr;

HRESULT initCOM();
void closeCOM();
ComPtr<IPortableDeviceManager> getDeviceManager();
ComPtr<IPortableDeviceValues> getClientInfo();
ComPtr<IPortableDevice> getSelectedDevice(PWSTR id);
vector<MTPDevice> getDevices();

string formatHR(HRESULT hr);
PWSTR getDeviceDescription(PWSTR deviceId);
PWSTR getDeviceFriendlyName(PWSTR deviceId);
PWSTR getDeviceManufacturer(PWSTR deviceId);