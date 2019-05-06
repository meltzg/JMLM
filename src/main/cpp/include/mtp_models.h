#pragma once
#include <string>

namespace jmtp
{
struct MTPDeviceIdInfo
{
    uint16_t vendor_id;
    uint16_t product_id;
    std::wstring serial;
};

struct MTPDeviceInfo
{
    std::wstring device_id;
    std::wstring friendly_name;
    std::wstring description;
    std::wstring manufacturer;

    MTPDeviceIdInfo id_info;

    uint32_t busLocation;
    uint8_t devNum;
};

struct MTPStorageDevice
{
    std::wstring id;
    unsigned long long capacity;
};
} // namespace jmtp
//uint32_t bus_location, uint8_t devnum, uint16_t product_id, uint16_t vendor_id