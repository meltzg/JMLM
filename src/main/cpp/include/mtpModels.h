#pragma once
#include <string>

using std::wstring;

class MTPContentNode {

};

struct MTPDeviceInfo {
	wstring deviceId;
	wstring friendlyName;
	wstring description;
	wstring manufacturer;
};