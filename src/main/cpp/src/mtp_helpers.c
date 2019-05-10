#include <libmtp.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include "mtp_helpers.h"
// #include "common_helpers.h"

char *toIDStr(MTPDeviceIdInfo info)
{
    char buffer[1024];
    sprintf(buffer, "%d:%d:%s", info.vendor_id, info.product_id, info.serial);
    unsigned int idLen = strlen(buffer);
    char *idStr = malloc((idLen + 1) * idLen);
    strcpy(idStr, buffer);

    return idStr;
}

// MTPDeviceIdInfo fromIDStr(wstring str)
// {
//     MTPDeviceIdInfo info;

//     wstring tmp;
//     wchar_t *pEnd;
//     wstringstream wss(str);

//     std::getline(wss, tmp, L':');
//     info.vendor_id = wcstol(tmp.c_str(), &pEnd, 10);
//     std::getline(wss, tmp, L':');
//     info.product_id = wcstol(tmp.c_str(), &pEnd, 10);
//     std::getline(wss, tmp, L':');
//     info.serial = tmp.c_str();

//     return info;
// }

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

// int toDeviceFileId(wstring path, LIBMTP_mtpdevice_t *device)
// {
// }

// void getOpenDevice(wstring id, LIBMTP_mtpdevice_t **device, uint32_t *busLocation, uint8_t *devNum)
// {
//     LIBMTP_raw_device_t *raw_devs = nullptr;
//     LIBMTP_mtpdevice_t *open_device = nullptr;
//     LIBMTP_error_number_t ret;
//     int numdevs;

//     ret = LIBMTP_Detect_Raw_Devices(&raw_devs, &numdevs);

//     if (ret == LIBMTP_ERROR_NONE && numdevs > 0)
//     {
//         for (int i = 0; i < numdevs; i++)
//         {
//             LIBMTP_mtpdevice_t *found_device = LIBMTP_Open_Raw_Device_Uncached(&raw_devs[i]);
//             char *serial = LIBMTP_Get_Serialnumber(found_device);
//             MTPDeviceIdInfo id_info;
//             id_info.vendor_id = raw_devs[i].device_entry.vendor_id;
//             id_info.product_id = raw_devs[i].device_entry.product_id;
//             id_info.serial = charToWString(serial);
//             free(serial);

//             wstring found_id = toIDStr(id_info);
//             if (found_id.compare(id.c_str()))
//             {
//                 *device = found_device;
//                 *busLocation = raw_devs[i].bus_location;
//                 *devNum = raw_devs[i].devnum;
//                 break;
//             }
//             else
//             {
//                 LIBMTP_Release_Device(found_device);
//             }
//         }
//     }

//     free(raw_devs);
// }

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

// optional<MTPDeviceInfo> getDeviceInfo(wstring id)
// {
//     LIBMTP_mtpdevice_t *device = nullptr;
//     uint32_t busLocation;
//     uint8_t devNum;
//     getOpenDevice(id, &device, &busLocation, &devNum);

//     if (device == nullptr)
//     {
//         return std::nullopt;
//     }

//     MTPDeviceIdInfo id_info = fromIDStr(id);
//     MTPDeviceInfo device_info = toMTPDeviceInfo(device, busLocation, devNum, id_info.vendor_id, id_info.product_id);
//     LIBMTP_Release_Device(device);
//     return device_info;
// }

// optional<MTPStorageDevice> getStorageDevice(wstring device_id, wstring path)
// {
//     LIBMTP_mtpdevice_t *device = nullptr;
//     uint32_t busLocation;
//     uint8_t devNum;
//     getOpenDevice(device_id, &device, &busLocation, &devNum);

//     if (device == nullptr)
//     {
//         return std::nullopt;
//     }

//     LIBMTP_Release_Device(device);
//     return std::nullopt;
// }
// } // namespace jmtp