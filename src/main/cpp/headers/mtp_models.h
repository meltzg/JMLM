#pragma once
#include <libmtp.h>

struct MTPDeviceIdInfo
{
    uint16_t vendor_id;
    uint16_t product_id;
    char *serial;
};

struct MTPDeviceInfo
{
    char *device_id;
    char *friendly_name;
    char *description;
    char *manufacturer;

    MTPDeviceIdInfo id_info;

    uint32_t busLocation;
    uint8_t devNum;
};

struct MTPStorageDevice
{
    char *id;
    long capacity;
};
