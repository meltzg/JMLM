#pragma once
#include <string>

using std::wstring;

namespace LibJMTP {

	struct MTPContentNode {
		wstring id;
		wstring pId;
		wstring origName;
		wstring name;
		bool isDir;
		unsigned long long size;
		unsigned long long capacity;

		bool isValid;
	};

	struct MTPDeviceInfo {
		wstring deviceId;
		wstring friendlyName;
		wstring description;
		wstring manufacturer;
	};
}