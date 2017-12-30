#pragma once
#include <vector>
#include <string>
#include "mtpModels.h"

using std::vector;
using std::wstring;

namespace LibJMTP {
	vector<MTPDeviceInfo> getDevicesInfo();
	MTPDeviceInfo getDeviceInfo(wstring id);

    vector<wstring> getChildIds(wstring deviceId, wstring parentId);
    MTPContentNode createDirNode(wstring deviceId, wstring parentId, wstring name);
    MTPContentNode createContentNode(wstring deviceId, wstring parentId, wstring file);
    MTPContentNode readNode(wstring deviceId, wstring id);
    MTPContentNode copyNode(wstring deviceId, wstring parentId, wstring id, wstring tmpFolder);
    bool deleteNode(wstring deviceId, wstring id);
    bool retrieveNode(wstring deviceId, wstring id, wstring destFolder);
}
