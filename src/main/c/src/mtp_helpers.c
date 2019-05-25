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

bool getOpenDevice(MTPDeviceInfo *deviceInfo, const char *deviceId, LIBMTP_mtpdevice_t **device, LIBMTP_raw_device_t **raw_devs, uint32_t *busLocation, uint8_t *devNum)
{
    *device = NULL;
    *raw_devs = NULL;

    LIBMTP_mtpdevice_t *open_device = NULL;
    LIBMTP_error_number_t ret;
    int numdevs;
    ret = LIBMTP_Detect_Raw_Devices(raw_devs, &numdevs);
    if (ret == LIBMTP_ERROR_NONE && numdevs > 0)
    {
        for (int i = 0; i < numdevs; i++)
        {
            LIBMTP_mtpdevice_t *found_device = LIBMTP_Open_Raw_Device_Uncached(raw_devs[i]);
            char *serial = LIBMTP_Get_Serialnumber(found_device);
            MTPDeviceIdInfo id_info;
            id_info.vendor_id = (*raw_devs)[i].device_entry.vendor_id;
            id_info.product_id = (*raw_devs)[i].device_entry.product_id;
            id_info.serial = serial;

            if (strcmp(toIDStr(id_info), deviceId) == 0)
            {
                *device = found_device;
                *busLocation = (*raw_devs)[i].bus_location;
                *devNum = (*raw_devs)[i].devnum;
                toMTPDeviceInfo(deviceInfo, found_device, *busLocation, *devNum, id_info.vendor_id, id_info.product_id);
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

    return numdevs;
}

bool getDeviceInfo(MTPDeviceInfo *deviceInfo, const char *deviceId)
{
    LIBMTP_mtpdevice_t *device = NULL;
    LIBMTP_raw_device_t *rawdevices = NULL;
    uint32_t busLocation;
    uint8_t devNum;
    bool ret = false;

    if (getOpenDevice(deviceInfo, deviceId, &device, &rawdevices, &busLocation, &devNum))
    {
        LIBMTP_Release_Device(device);
        ret = true;
    }
    if (rawdevices != NULL)
    {
        free(rawdevices);
    }
    return ret;
}

void initMTP()
{
    LIBMTP_Init();
}

bool getStorageDevice(MTPStorageDevice *storageDevice, const char *device_id, const char *path)
{
    LIBMTP_mtpdevice_t *device;
    LIBMTP_raw_device_t *rawdevices;

    uint32_t busLocation;
    uint8_t devNum;
    MTPDeviceInfo deviceInfo;
    bool ret = false;

    if (getOpenDevice(&deviceInfo, device_id, &device, &rawdevices, &busLocation, &devNum))
    {
        LIBMTP_devicestorage_t *storage;
        storageDevice->storage_id = NULL;
        storageDevice->capacity = 0;
        storageDevice->free_space = 0;

        char *pathCopy = malloc(sizeof(char) * (strlen(path) + 1));
        strcpy(pathCopy, path);
        char *pathPart = strtok(pathCopy, "/");

        for (storage = device->storage; storage != 0 && pathPart != NULL; storage = storage->next)
        {
            if (strcmp(storage->StorageDescription, pathPart) == 0)
            {
                storageDevice->storage_id = malloc(sizeof(char) * 20);
                sprintf(storageDevice->storage_id, "%#lx", storage->id);

                storageDevice->free_space = storage->FreeSpaceInBytes;
                storageDevice->capacity = storage->MaxCapacity;

                ret = true;
            }
        }

        LIBMTP_Release_Device(device);
        free(pathCopy);
    }

    if (rawdevices != NULL)
    {
        free(rawdevices);
    }

    return ret;
}
