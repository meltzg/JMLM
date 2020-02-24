#include <libmtp.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
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

char *getStorageDeviceId(const char *device_id, const char *path)
{
    LIBMTP_mtpdevice_t *device;
    LIBMTP_raw_device_t *rawdevices;

    uint32_t busLocation;
    uint8_t devNum;
    MTPDeviceInfo deviceInfo;
    char *storage_id = NULL;

    if (getOpenDevice(&deviceInfo, device_id, &device, &rawdevices, &busLocation, &devNum))
    {
        LIBMTP_devicestorage_t *storage;

        char *pathCopy = malloc(sizeof(char) * (strlen(path) + 1));
        strcpy(pathCopy, path);
        char *pathPart = strtok(pathCopy, "/");

        for (storage = device->storage; storage != 0 && pathPart != NULL; storage = storage->next)
        {
            if (strcmp(storage->StorageDescription, pathPart) == 0)
            {
                storage_id = malloc(sizeof(char) * 20);
                sprintf(storage_id, "%#lx", storage->id);
                break;
            }
        }

        LIBMTP_Release_Device(device);
        free(pathCopy);
    }

    if (rawdevices != NULL)
    {
        free(rawdevices);
    }

    return storage_id;
}

bool getStorageDevice(MTPStorageDevice *storageDevice, const char *device_id, const char *storage_id)
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

        for (storage = device->storage; storage != 0; storage = storage->next)
        {
            if (strtoul(storage_id, NULL, 0) == storage->id)
            {
                storageDevice->storage_id = malloc(sizeof(char) * 20);
                sprintf(storageDevice->storage_id, "%#lx", storage->id);

                storageDevice->free_space = storage->FreeSpaceInBytes;
                storageDevice->capacity = storage->MaxCapacity;

                ret = true;
                break;
            }
        }

        LIBMTP_Release_Device(device);
    }

    if (rawdevices != NULL)
    {
        free(rawdevices);
    }

    return ret;
}

LIBMTP_file_t *getFile(const LIBMTP_mtpdevice_t *device, LIBMTP_devicestorage_t *storage, uint32_t parentId, const char *filepath)
{
    LIBMTP_file_t *file = LIBMTP_Get_Files_And_Folders(device, storage->id, parentId);
    LIBMTP_file_t *foundFile = NULL;
    if (file == NULL)
    {
        return NULL;
    }
    char *slash = strchr(filepath, '/');
    char *restOfPath = NULL;
    if (slash != NULL)
    {
        *slash = '\0';
        restOfPath = slash + 1;
    }
    while (file && !foundFile)
    {
        if (strcmp(filepath, file->filename) == 0)
        {
            if (restOfPath == NULL)
            {
                foundFile = file;
            }
            else
            {
                foundFile = getFile(device, storage, file->item_id, restOfPath);
            }
            break;
        }
        LIBMTP_file_t *prevFile = file;
        file = file->next;
        LIBMTP_destroy_file_t(prevFile);
    }
    return foundFile;
}

LIBMTP_file_t *findFile(const LIBMTP_mtpdevice_t *device, const char *path)
{
    char *pathCopy = malloc(sizeof(char) * (strlen(path) + 1));
    strcpy(pathCopy, path);
    int copyOffset = 0;

    if (strlen(pathCopy) && pathCopy[0] == '/')
    {
        pathCopy++;
        copyOffset = 1;
    }

    char *slash = strchr(pathCopy, '/');
    LIBMTP_file_t *foundFile = NULL;
    if (slash != NULL)
    {
        *slash = '\0';
        char *storageDescription = pathCopy;
        char *restOfPath = slash + 1;

        for (LIBMTP_devicestorage_t *storage = device->storage; storage != 0 && storageDescription != NULL; storage = storage->next)
        {
            if (strcmp(storage->StorageDescription, storageDescription) == 0)
            {
                foundFile = getFile(device, storage, LIBMTP_FILES_AND_FOLDERS_ROOT, restOfPath);
                break;
            }
        }
    }

    pathCopy -= copyOffset;
    free(pathCopy);

    return foundFile;
}

struct FileContentWrapper
{
    uint64_t pos;
    uint8_t *buf;
};

uint16_t copyFileContent(void *params, void *output, uint32_t sendlen, unsigned char *data, uint32_t *putlen)
{
    // fwrite(data, sizeof(char), sendlen, output);
    // memcpy(output, data, sendlen);
    struct FileContentWrapper *w = (struct FileContentWrapper *)output;
    memcpy(w->buf + w->pos, data, sendlen);
    *putlen = sendlen;
    return LIBMTP_HANDLER_RETURN_OK;
}

uint8_t *getFileContent(const char *device_id, const char *path, uint64_t *size)
{
    LIBMTP_mtpdevice_t *device;
    LIBMTP_raw_device_t *rawdevices;

    uint32_t busLocation;
    uint8_t devNum;
    MTPDeviceInfo deviceInfo;

    uint8_t *output = NULL;
    *size = 0;

    if (getOpenDevice(&deviceInfo, device_id, &device, &rawdevices, &busLocation, &devNum))
    {
        LIBMTP_file_t *foundFile = findFile(device, path);

        if (foundFile != NULL)
        {
            if (foundFile->filetype == LIBMTP_FILETYPE_FOLDER)
            {
                printf("%s is a directory", path);
            }
            else
            {
                output = malloc(foundFile->filesize);
                char buffer[L_tmpnam];
                tmpnam(buffer);
                int ret = LIBMTP_Get_File_To_File(device, foundFile->item_id, buffer, NULL, NULL);
                if (ret != 0)
                {
                    printf("file content retrieval failed: %04x\n", ret);
                    output = NULL;
                }
                else
                {
                    FILE *stream = fopen(buffer, "rb");
                    fread(output, 1, foundFile->filesize, stream);
                    fclose(stream);
                    remove(buffer);
                    *size = foundFile->filesize;
                }
            }
            LIBMTP_destroy_file_t(foundFile);
        }

        LIBMTP_Release_Device(device);
    }

    if (rawdevices != NULL)
    {
        free(rawdevices);
    }

    return output;
}

bool isDirectory(const char *device_id, const char *path)
{
    LIBMTP_mtpdevice_t *device;
    LIBMTP_raw_device_t *rawdevices;

    uint32_t busLocation;
    uint8_t devNum;
    MTPDeviceInfo deviceInfo;

    bool isDir = false;

    if (getOpenDevice(&deviceInfo, device_id, &device, &rawdevices, &busLocation, &devNum))
    {
        LIBMTP_file_t *foundFile = findFile(device, path);
        if (foundFile != NULL && foundFile->filetype == LIBMTP_FILETYPE_FOLDER)
        {
            isDir = true;
            LIBMTP_destroy_file_t(foundFile);
        }
        LIBMTP_Release_Device(device);
    }

    return isDir;
}
