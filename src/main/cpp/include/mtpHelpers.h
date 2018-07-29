#pragma once
#include <vector>
#include <string>
#include <optional>
#include "mtpModels.h"

namespace LibJMTP {
	const unsigned int NUM_OBJECTS_TO_REQUEST = 100;

	std::vector<MTPDeviceInfo> getDevicesInfo();
	std::optional<MTPDeviceInfo> getDeviceInfo(std::wstring id);
    void initMTP();

    std::vector<std::wstring> getChildIds(std::wstring deviceId, std::wstring parentId);
    MTPContentNode createDirNode(std::wstring deviceId, std::wstring parentId, std::wstring name);
    MTPContentNode createContentNode(std::wstring deviceId, std::wstring parentId, std::wstring file);
    MTPContentNode readNode(std::wstring deviceId, std::wstring id);
    MTPContentNode copyNode(std::wstring deviceId, std::wstring parentId, std::wstring id, std::wstring tmpFolder);
    bool deleteNode(std::wstring deviceId, std::wstring id);
    bool retrieveNode(std::wstring deviceId, std::wstring id, std::wstring destFolder);
}
