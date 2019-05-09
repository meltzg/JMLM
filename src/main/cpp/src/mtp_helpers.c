// #include <libmtp.h>
// #include "mtp_helpers.h"
// #include "common_helpers.h"

// wstring toIDStr(MTPDeviceIdInfo info)
// {
//     wstringstream wss;
//     wss << info.vendor_id
//         << L":" << info.product_id
//         << L":" << info.serial;
//     return wss.str();
// }

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

// MTPDeviceInfo toMTPDeviceInfo(LIBMTP_mtpdevice_t *device, uint32_t busLocation, uint8_t devNum, uint16_t vendor_id, uint16_t product_id)
// {
//     char *serial = LIBMTP_Get_Serialnumber(device);
//     char *friendly = LIBMTP_Get_Friendlyname(device);
//     char *description = LIBMTP_Get_Modelname(device);
//     char *manufacturer = LIBMTP_Get_Manufacturername(device);
//     MTPDeviceIdInfo id_info;
//     id_info.vendor_id = vendor_id;
//     id_info.product_id = product_id;
//     id_info.serial = charToWString(serial);

//     MTPDeviceInfo device_info;

//     device_info.device_id = toIDStr(id_info);
//     device_info.friendly_name = charToWString(friendly);
//     device_info.description = charToWString(description);
//     device_info.manufacturer = charToWString(manufacturer);

//     device_info.id_info = id_info;
//     device_info.busLocation = busLocation;
//     device_info.devNum = devNum;

//     free(serial);
//     free(friendly);
//     free(description);
//     free(manufacturer);

//     return device_info;
// }

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

// vector<MTPDeviceInfo> getDevicesInfo()
// {
//     vector<MTPDeviceInfo> devices;
//     LIBMTP_raw_device_t *raw_devs = nullptr;
//     LIBMTP_error_number_t ret;
//     int numdevs;

//     ret = LIBMTP_Detect_Raw_Devices(&raw_devs, &numdevs);

//     if (ret == LIBMTP_ERROR_NONE && numdevs > 0)
//     {
//         for (int i = 0; i < numdevs; i++)
//         {
//             LIBMTP_mtpdevice_t *device = LIBMTP_Open_Raw_Device_Uncached(&raw_devs[i]);
//             MTPDeviceInfo device_info = toMTPDeviceInfo(device,
//                                                         raw_devs[i].bus_location,
//                                                         raw_devs[i].devnum,
//                                                         raw_devs[i].device_entry.vendor_id,
//                                                         raw_devs[i].device_entry.product_id);
//             devices.push_back(device_info);

//             LIBMTP_Release_Device(device);
//         }
//     }
//     free(raw_devs);
//     return devices;
// }

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