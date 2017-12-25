#pragma once
#include <string>

using std::wstring;

namespace LibJMTP {

	class MTPContentNode {

	};

	struct MTPDeviceInfo {
		wstring deviceId;
		wstring friendlyName;
		wstring description;
		wstring manufacturer;
	};
}