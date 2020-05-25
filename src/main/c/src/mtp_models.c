#include <stdlib.h>
#include "mtp_models.h"

void freeMTPDeviceIdInfo(MTPDeviceIdInfo_t deviceIdInfo)
{
    if (deviceIdInfo.serial != NULL)
    {
        free(deviceIdInfo.serial);
    }
}

void freeMTPDeviceInfo(MTPDeviceInfo_t deviceInfo)
{
    if (deviceInfo.device_id != NULL)
    {
        free(deviceInfo.device_id);
    }
    if (deviceInfo.friendly_name != NULL)
    {
        free(deviceInfo.friendly_name);
    }
    if (deviceInfo.description != NULL)
    {
        free(deviceInfo.description);
    }
    if (deviceInfo.manufacturer != NULL)
    {
        free(deviceInfo.manufacturer);
    }

    freeMTPDeviceIdInfo(deviceInfo.id_info);
}

void freeMTPStorageDevice(MTPStorageDevice_t storageDevice)
{
    if (storageDevice.storage_id != NULL)
    {
        free(storageDevice.storage_id);
    }
}
