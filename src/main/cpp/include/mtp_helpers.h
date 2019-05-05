#pragma once
#include <vector>
#include <string>
#include <optional>
#include "mtp_models.h"

namespace jmtp
{
const unsigned int NUM_OBJECTS_TO_REQUEST = 100;

std::optional<std::wstring> toDeviceId(uint32_t bus_location, uint8_t devnum, uint16_t product_id, uint16_t vendor_id);

std::vector<MTPDeviceInfo> getDevicesInfo();
std::optional<MTPDeviceInfo> getDeviceInfo(std::wstring id);
void initMTP();

std::optional<MTPStorageDevice> getStorageDevice(std::wstring device_id, std::wstring path);
} // namespace jmtp