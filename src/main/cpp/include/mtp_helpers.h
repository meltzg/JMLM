#pragma once
#include <vector>
#include <string>
#include <optional>
#include "mtp_models.h"

namespace jmtp {
	const unsigned int NUM_OBJECTS_TO_REQUEST = 100;

	std::vector<MTPDeviceInfo> getDevicesInfo();
	std::optional<MTPDeviceInfo> getDeviceInfo(std::wstring id);
    void initMTP();

    std::vector<std::wstring> getChildIds(std::wstring device_id, std::wstring parentId);
    std::optional<MTPContentNode> createDirNode(std::wstring device_id, std::wstring parentId, std::wstring name);
    std::optional<MTPContentNode> createContentNode(std::wstring device_id, std::wstring parentId, std::wstring file);
    std::optional<MTPContentNode> readNode(std::wstring device_id, std::wstring id);
    std::optional<MTPContentNode> copyNode(std::wstring device_id, std::wstring parentId, std::wstring id, std::wstring tmpFolder);
    bool deleteNode(std::wstring device_id, std::wstring id);
    bool retrieveNode(std::wstring device_id, std::wstring id, std::wstring destFolder);
}
