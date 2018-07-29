#pragma once
#include <string>

namespace LibJMTP {

	struct MTPContentNode {
		std::wstring id;
		std::wstring pId;
		std::wstring origName;
		std::wstring name;
		bool isDir;
		unsigned long long size;
		unsigned long long capacity;

		bool isValid;

		MTPContentNode() { isValid = false; }
	};

	struct MTPDeviceInfo {
		std::wstring deviceId;
		std::wstring friendlyName;
		std::wstring description;
		std::wstring manufacturer;
	};
}