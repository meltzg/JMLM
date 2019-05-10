#include <stdlib.h>
#include "mtp_models.h"

void freeMTPDeviceIdInfo(MTPDeviceIdInfo deviceIdInfo)
{
    if (deviceIdInfo.serial != NULL)
    {
        free(deviceIdInfo.serial);
    }
}

void freeMTPDeviceInfo(MTPDeviceInfo deviceInfo)
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

void freeMTPStorageDevice(MTPStorageDevice storageDevice)
{
    if (storageDevice.id != NULL)
    {
        free(storageDevice.id);
    }
}
