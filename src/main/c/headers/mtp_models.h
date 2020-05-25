#ifndef MTP_MODELS_H
#define MTP_MODELS_H

#include <libmtp.h>

typedef struct MTPDeviceIdInfo_struct MTPDeviceIdInfo_t;
typedef struct MTPDeviceInfo_struct MTPDeviceInfo_t;
typedef struct MTPStorageDevice_struct MTPStorageDevice_t;

struct MTPDeviceIdInfo_struct
{
    uint16_t vendor_id;
    uint16_t product_id;
    char *serial;
};

struct MTPDeviceInfo_struct
{
    char *device_id;
    char *friendly_name;
    char *description;
    char *manufacturer;

    MTPDeviceIdInfo_t id_info;

    uint32_t busLocation;
    uint8_t devNum;
};

struct MTPStorageDevice_struct
{
    char *storage_id;
    uint64_t capacity;
    uint64_t free_space;
};

void freeMTPDeviceIdInfo(MTPDeviceIdInfo_t deviceIdInfo);
void freeMTPDeviceInfo(MTPDeviceInfo_t deviceInfo);
void freeMTPStorageDevice(MTPStorageDevice_t storageDevice);

#endif
