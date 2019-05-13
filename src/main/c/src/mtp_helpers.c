#include <libmtp.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include "mtp_helpers.h"

char *toIDStr(MTPDeviceIdInfo info)
{
    char buffer[1024];
    sprintf(buffer, "%d:%d:%s", info.vendor_id, info.product_id, info.serial);
    unsigned int idLen = strlen(buffer);
    char *idStr = malloc((idLen + 1) * idLen);
    strcpy(idStr, buffer);

    return idStr;
}

void toMTPDeviceInfo(MTPDeviceInfo *device_info, LIBMTP_mtpdevice_t *device, uint32_t busLocation, uint8_t devNum, uint16_t vendor_id, uint16_t product_id)
{
    char *serial = LIBMTP_Get_Serialnumber(device);
    char *friendly = LIBMTP_Get_Friendlyname(device);
    char *description = LIBMTP_Get_Modelname(device);
    char *manufacturer = LIBMTP_Get_Manufacturername(device);

    device_info->id_info.vendor_id = vendor_id;
    device_info->id_info.product_id = product_id;
    device_info->id_info.serial = serial;

    device_info->device_id = toIDStr(device_info->id_info);
    device_info->friendly_name = friendly;
    device_info->description = description;
    device_info->manufacturer = manufacturer;

    device_info->busLocation = busLocation;
    device_info->devNum = devNum;
}

bool getOpenDevice(MTPDeviceInfo *deviceInfo, const char *deviceId, LIBMTP_mtpdevice_t **device, uint32_t *busLocation, uint8_t *devNum)
{
    LIBMTP_raw_device_t *raw_devs = NULL;
    LIBMTP_mtpdevice_t *open_device = NULL;
    LIBMTP_error_number_t ret;
    int numdevs;
    ret = LIBMTP_Detect_Raw_Devices(&raw_devs, &numdevs);
    if (ret == LIBMTP_ERROR_NONE && numdevs > 0)
    {
        for (int i = 0; i < numdevs; i++)
        {
            LIBMTP_mtpdevice_t *found_device = LIBMTP_Open_Raw_Device_Uncached(&raw_devs[i]);
            char *serial = LIBMTP_Get_Serialnumber(found_device);
            MTPDeviceIdInfo id_info;
            id_info.vendor_id = raw_devs[i].device_entry.vendor_id;
            id_info.product_id = raw_devs[i].device_entry.product_id;
            id_info.serial = serial;

            if (strcmp(toIDStr(id_info), deviceId) == 0)
            {
                *device = found_device;
                *busLocation = raw_devs[i].bus_location;
                *devNum = raw_devs[i].devnum;
                toMTPDeviceInfo(deviceInfo, found_device, *busLocation, *devNum, id_info.vendor_id, id_info.product_id);
                // free(raw_devs);
                return true;
            }
            LIBMTP_Release_Device(found_device);
        }
    }
    free(raw_devs);
    return false;
}

int getDevicesInfo(MTPDeviceInfo **devices)
{
    LIBMTP_raw_device_t *raw_devs = NULL;
    LIBMTP_error_number_t ret;
    int numdevs;

    ret = LIBMTP_Detect_Raw_Devices(&raw_devs, &numdevs);

    if (ret == LIBMTP_ERROR_NONE && numdevs > 0)
    {
        *devices = malloc(numdevs * sizeof(MTPDeviceInfo));
        for (int i = 0; i < numdevs; i++)
        {
            LIBMTP_mtpdevice_t *device = LIBMTP_Open_Raw_Device_Uncached(&raw_devs[i]);
            toMTPDeviceInfo(&((*devices)[i]),
                            device,
                            raw_devs[i].bus_location,
                            raw_devs[i].devnum,
                            raw_devs[i].device_entry.vendor_id,
                            raw_devs[i].device_entry.product_id);

            LIBMTP_Release_Device(device);
        }
    }
    if (raw_devs != NULL)
    {
        free(raw_devs);
    }

    return numdevs;
}

bool getDeviceInfo(MTPDeviceInfo *deviceInfo, const char *deviceId)
{
    LIBMTP_mtpdevice_t *device = NULL;
    uint32_t busLocation;
    uint8_t devNum;

    if (getOpenDevice(deviceInfo, deviceId, &device, &busLocation, &devNum))
    {
        LIBMTP_Release_Device(device);
        return true;
    }

    return false;
}

static uint32_t
lookup_folder_id(LIBMTP_folder_t *folder, char *path, char *parent)
{
    char *current;
    uint32_t ret = (uint32_t)-1;

    if (strcmp(path, "/") == 0)
        return 0;

    if (folder == NULL)
    {
        return ret;
    }

    current = malloc(strlen(parent) + strlen(folder->name) + 2);
    sprintf(current, "%s/%s", parent, folder->name);
    if (strcasecmp(path, current) == 0)
    {
        free(current);
        return folder->folder_id;
    }
    if (strncasecmp(path, current, strlen(current)) == 0)
    {
        ret = lookup_folder_id(folder->child, path, current);
    }
    free(current);
    if (ret != (uint32_t)(-1))
    {
        return ret;
    }
    ret = lookup_folder_id(folder->sibling, path, parent);
    return ret;
}

bool getStorageDevice(MTPStorageDevice *storageDevice, const char *device_id, const char *path)
{
    LIBMTP_mtpdevice_t *device = NULL;
    uint32_t busLocation;
    uint8_t devNum;
    MTPDeviceInfo deviceInfo;

    if (!getOpenDevice(&deviceInfo, device_id, &device, &busLocation, &devNum))
    {
        return false;
    }

    LIBMTP_folder_t *folders = LIBMTP_Get_Folder_List(device);

    uint32_t folderId = lookup_folder_id(folders, path, "");
    printf("Folder ID %d\n", folderId);

    storageDevice->storage_id = NULL;
    storageDevice->capacity = 0;
    storageDevice->free_space = 0;

    LIBMTP_Release_Device(device);
    return true;
}
