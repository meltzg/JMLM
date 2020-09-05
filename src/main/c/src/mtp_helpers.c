#define _GNU_SOURCE
#include <libmtp.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/mman.h>
#include "mtp_helpers.h"

char *toIDStr(MTPDeviceIdInfo_t info)
{
    char buffer[1024];
    sprintf(buffer, "%d:%d:%s", info.vendor_id, info.product_id, info.serial);
    unsigned int idLen = strlen(buffer);
    char *idStr = malloc((idLen + 1) * idLen);
    strcpy(idStr, buffer);

    return idStr;
}

void toMTPDeviceInfo(MTPDeviceInfo_t *device_info, LIBMTP_mtpdevice_t *device, uint32_t busLocation, uint8_t devNum, uint16_t vendor_id, uint16_t product_id)
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

bool getOpenDevice(MTPDeviceInfo_t *deviceInfo, const char *deviceId, LIBMTP_mtpdevice_t **device, LIBMTP_raw_device_t **raw_devs, uint32_t *busLocation, uint8_t *devNum)
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
            MTPDeviceIdInfo_t id_info;
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

int getDevicesInfo(MTPDeviceInfo_t **devices)
{
    LIBMTP_raw_device_t *raw_devs = NULL;
    LIBMTP_error_number_t ret;
    int numdevs;

    ret = LIBMTP_Detect_Raw_Devices(&raw_devs, &numdevs);

    if (ret == LIBMTP_ERROR_NONE && numdevs > 0)
    {
        *devices = malloc(numdevs * sizeof(MTPDeviceInfo_t));
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

bool getDeviceInfo(MTPDeviceInfo_t *deviceInfo, const char *deviceId)
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

LIBMTP_devicestorage_t *getStorageDevice(const LIBMTP_mtpdevice_t *device, const char *path)
{
    LIBMTP_devicestorage_t *storage;
    LIBMTP_devicestorage_t *foundStorage = NULL;

    char *pathCopy = malloc(sizeof(char) * (strlen(path) + 1));
    strcpy(pathCopy, path);
    char *pathPart = strtok(pathCopy, "/");

    for (storage = device->storage; storage != 0 && pathPart != NULL; storage = storage->next)
    {
        if (strcmp(storage->StorageDescription, pathPart) == 0)
        {
            foundStorage = storage;
            break;
        }
    }

    free(pathCopy);

    return foundStorage;
}

char *getStorageDeviceId(const char *device_id, const char *path)
{
    LIBMTP_mtpdevice_t *device;
    LIBMTP_raw_device_t *rawdevices;

    uint32_t busLocation;
    uint8_t devNum;
    MTPDeviceInfo_t deviceInfo;
    char *storage_id = NULL;

    if (getOpenDevice(&deviceInfo, device_id, &device, &rawdevices, &busLocation, &devNum))
    {
        LIBMTP_devicestorage_t *storage = getStorageDevice(device, path);

        if (storage != NULL)
        {
            storage_id = malloc(sizeof(char) * 20);
            sprintf(storage_id, "%#lx", storage->id);
        }

        LIBMTP_Release_Device(device);
    }

    if (rawdevices != NULL)
    {
        free(rawdevices);
    }

    return storage_id;
}

bool getStorageDeviceMetadata(MTPStorageDevice_t *storageDevice, const char *device_id, const char *storage_id)
{
    LIBMTP_mtpdevice_t *device;
    LIBMTP_raw_device_t *rawdevices;

    uint32_t busLocation;
    uint8_t devNum;
    MTPDeviceInfo_t deviceInfo;
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

bool _isDirectory(const LIBMTP_mtpdevice_t *device, const char *path)
{
    bool isDir = false;

    if (strcmp(path, "/") == 0)
    {
        isDir = true;
    }
    else
    {
        LIBMTP_file_t *foundFile = findFile(device, path);
        if (foundFile != NULL && foundFile->filetype == LIBMTP_FILETYPE_FOLDER)
        {
            isDir = true;
            LIBMTP_destroy_file_t(foundFile);
        }
        else if (foundFile == NULL)
        {
            // if its not a directory, it might be storage
            LIBMTP_devicestorage_t *storage = getStorageDevice(device, path);
            if (storage != NULL && strcmp(path + 1, storage->StorageDescription) == 0)
            {
                isDir = true;
            }
        }
    }

    return isDir;
}

bool _deleteMtpFileRecursive(const LIBMTP_mtpdevice_t *device, LIBMTP_file_t *file)
{
    bool success = false;

    LIBMTP_file_t *children = LIBMTP_Get_Files_And_Folders(device, file->storage_id, file->item_id);
    if (children != NULL)
    {
        LIBMTP_file_t *child = children;
        while (child != NULL)
        {
            LIBMTP_file_t *oldChild = child;
            child = child->next;
            _deleteMtpFileRecursive(device, oldChild);
        }
    }

    int ret = LIBMTP_Delete_Object(device, file->item_id);
    LIBMTP_destroy_file_t(file);

    return ret == 0;
}

bool _deletePath(const LIBMTP_mtpdevice_t *device, const char *path)
{
    bool success = false;
    LIBMTP_devicestorage_t *storage = getStorageDevice(device, path);
    if (strcmp(path, "/") == 0)
    {
        printf("Cannot delete root path");
    }
    else if (storage != NULL && strcmp(path + 1, storage->StorageDescription) == 0)
    {
        printf("Cannot delete storage device");
    }
    else
    {
        LIBMTP_file_t *foundFile = findFile(device, path);
        success = _deleteMtpFileRecursive(device, foundFile);
    }
    return success;
}

uint8_t *getFileContent(const char *device_id, const char *path, uint64_t *size)
{
    LIBMTP_mtpdevice_t *device;
    LIBMTP_raw_device_t *rawdevices;

    uint32_t busLocation;
    uint8_t devNum;
    MTPDeviceInfo_t deviceInfo;

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
                int fd = memfd_create("mem_fd", MFD_CLOEXEC);
                int ret = LIBMTP_Get_File_To_File_Descriptor(device, foundFile->item_id, fd, NULL, NULL);
                if (ret != 0)
                {
                    printf("file content retrieval failed: %04x\n", ret);
                    output = NULL;
                }
                else
                {
                    lseek(fd, 0, SEEK_SET);
                    read(fd, output, foundFile->filesize);
                    close(fd);
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

int writeFileContent(const char *device_id, const char *path, const char *content, long offset, int size)
{
    LIBMTP_mtpdevice_t *device;
    LIBMTP_raw_device_t *rawdevices;

    uint32_t busLocation;
    uint8_t devNum;
    MTPDeviceInfo_t deviceInfo;

    int bytesWritten = -1;
    bool success = getOpenDevice(&deviceInfo, device_id, &device, &rawdevices, &busLocation, &devNum);
    char *lastSlash = NULL;
    LIBMTP_devicestorage_t *storageDevice = NULL;
    char *fileName = NULL;
    LIBMTP_file_t *existingFile = NULL;
    LIBMTP_file_t *parentDir = NULL;
    bool isStorage = false;

    if (success)
    {
        lastSlash = strrchr(path, '/');
        if (lastSlash == NULL)
        {
            printf("%s is not a valid path for file writing. Cannot write to device root", path);
            success = false;
        }
    }
    if (success)
    {
        storageDevice = getStorageDevice(device, path);
        if (storageDevice == NULL)
        {
            printf("%s is not a valid path for file writing. Cannot find valid storage device", path);
            success = false;
        }
        else if (_isDirectory(device, path))
        {
            printf("%s is not a valid path for file writing. Path is a directory", path);
            success = false;
        }
    }
    if (success)
    {
        existingFile = findFile(device, path);
        fileName = lastSlash + 1;
        *lastSlash = '\0';
        parentDir = findFile(device, path);

        isStorage = (strcmp(path, storageDevice->StorageDescription) == 0 || strcmp(path + 1, storageDevice->StorageDescription) == 0);

        if (parentDir == NULL && !isStorage)
        {
            printf("%s parent directory not found", path);
            success = false;
        }
    }
    if (success)
    {
        uint32_t storageId = storageDevice->id;
        u_int32_t parentId = isStorage ? LIBMTP_FILES_AND_FOLDERS_ROOT : parentDir->item_id;

        LIBMTP_file_t fileData;
        fileData.filename = fileName;
        fileData.parent_id = parentId;
        fileData.storage_id = storageId;
        fileData.item_id = 0;
        fileData.filesize = size;
        fileData.next = NULL;
        fileData.filetype = LIBMTP_FILETYPE_UNKNOWN;
        int fd = memfd_create("mem_fd", MFD_CLOEXEC);
        int ret = LIBMTP_ERROR_NONE;

        if (existingFile != NULL)
        {
            ret = LIBMTP_Get_File_To_File_Descriptor(device, existingFile->item_id, fd, NULL, NULL);
            LIBMTP_Delete_Object(device, existingFile->item_id);
        }
        if (ret == LIBMTP_ERROR_NONE)
        {
            lseek(fd, offset, SEEK_SET);
            write(fd, content, size);
            lseek(fd, 0, SEEK_SET);
            ret = LIBMTP_Send_File_From_File_Descriptor(device, fd, &fileData, NULL, NULL);
        }
        if (ret != LIBMTP_ERROR_NONE)
        {
            printf("could not write file to device");
            LIBMTP_Dump_Errorstack(device);
        }
        else
        {
            bytesWritten = size;
        }
        close(fd);
    }

    LIBMTP_destroy_file_t(existingFile);
    LIBMTP_destroy_file_t(parentDir);

    LIBMTP_Release_Device(device);

    if (rawdevices != NULL)
    {
        free(rawdevices);
    }

    return bytesWritten;
}

bool deletePath(const char *device_id, const char *path)
{
    LIBMTP_mtpdevice_t *device;
    LIBMTP_raw_device_t *rawdevices;

    uint32_t busLocation;
    uint8_t devNum;
    MTPDeviceInfo_t deviceInfo;

    bool success = false;

    if (getOpenDevice(&deviceInfo, device_id, &device, &rawdevices, &busLocation, &devNum))
    {
        success = _deletePath(device, path);

        LIBMTP_Release_Device(device);
    }

    if (rawdevices != NULL)
    {
        free(rawdevices);
    }

    return success;
}

bool isDirectory(const char *device_id, const char *path)
{
    LIBMTP_mtpdevice_t *device;
    LIBMTP_raw_device_t *rawdevices;

    uint32_t busLocation;
    uint8_t devNum;
    MTPDeviceInfo_t deviceInfo;

    bool isDir = false;

    if (getOpenDevice(&deviceInfo, device_id, &device, &rawdevices, &busLocation, &devNum))
    {
        isDir = _isDirectory(device, path);
    }
    if (rawdevices != NULL)
    {
        free(rawdevices);
    }
    return isDir;
}

long fileSize(const char *device_id, const char *path)
{
    LIBMTP_mtpdevice_t *device;
    LIBMTP_raw_device_t *rawdevices;

    uint32_t busLocation;
    uint8_t devNum;
    MTPDeviceInfo_t deviceInfo;

    long size = 0;

    if (getOpenDevice(&deviceInfo, device_id, &device, &rawdevices, &busLocation, &devNum))
    {
        if (!_isDirectory(device, path))
        {
            LIBMTP_file_t *foundFile = findFile(device, path);
            if (foundFile != NULL)
            {
                size = foundFile->filesize;
                LIBMTP_destroy_file_t(foundFile);
            }
            else
            {
                size = -1;
            }
            
        }

        LIBMTP_Release_Device(device);
    }

    return size;
}

char **getPathChildren(const char *device_id, const char *path, int *numChildren)
{
    LIBMTP_mtpdevice_t *device;
    LIBMTP_raw_device_t *rawdevices;

    uint32_t busLocation;
    uint8_t devNum;
    MTPDeviceInfo_t deviceInfo;

    char **childNames = NULL;

    if (getOpenDevice(&deviceInfo, device_id, &device, &rawdevices, &busLocation, &devNum))
    {
        if (_isDirectory(device, path))
        {
            *numChildren = 0;

            if (strcmp(path, "/") == 0)
            {
                // This is the root, children are storage devices
                LIBMTP_devicestorage_t *storage = device->storage;
                while (storage != NULL)
                {
                    (*numChildren)++;
                    storage = storage->next;
                }
                childNames = (char *)malloc(sizeof(char *) * (*numChildren));

                storage = device->storage;
                for (int i = 0; i < (*numChildren) && storage != NULL; i++)
                {
                    childNames[i] = malloc(sizeof(char) * strlen(storage->StorageDescription) + 1);
                    strcpy(childNames[i], storage->StorageDescription);
                    storage = storage->next;
                }
            }
            else
            {
                LIBMTP_file_t *foundDir = findFile(device, path);
                LIBMTP_file_t *children = NULL;

                if (foundDir != NULL)
                {
                    children = LIBMTP_Get_Files_And_Folders(device, foundDir->storage_id, foundDir->item_id);
                }
                else
                {
                    // could be storage isntead of an actual directory
                    LIBMTP_devicestorage_t *storage = getStorageDevice(device, path);
                    if (storage != NULL)
                    {
                        children = LIBMTP_Get_Files_And_Folders(device, storage->id, LIBMTP_FILES_AND_FOLDERS_ROOT);
                    }
                }

                if (children != NULL)
                {
                    LIBMTP_file_t *child = children;
                    while (child != NULL)
                    {
                        (*numChildren)++;
                        child = child->next;
                    }
                    childNames = (char *)malloc(sizeof(char *) * (*numChildren));
                    child = children;
                    for (int i = 0; i < (*numChildren) && child != NULL; i++)
                    {
                        childNames[i] = malloc(sizeof(char) * strlen(child->filename) + 1);
                        strcpy(childNames[i], child->filename);
                        LIBMTP_file_t *oldChild = child;
                        child = child->next;
                        LIBMTP_destroy_file_t(oldChild);
                    }
                }
                else
                {
                    childNames = (char *)malloc(0);
                }

                LIBMTP_destroy_file_t(foundDir);
            }
        }
        LIBMTP_Release_Device(device);

        return childNames;
    }
}
