#include <libmtp.h>
#include <sstream>
#include <cwchar>
#include "mtpHelpers.h"
#include "commonHelpers.h"

#include <iostream>

using std::endl;
using std::optional;
using std::vector;
using std::wcout;
using std::wstring;
using std::wstringstream;

namespace LibJMTP
{
struct MTPDeviceIdInfo
{
    uint16_t vendor_id;
    uint16_t product_id;
    wstring serial;
};

wstring toIDStr(MTPDeviceIdInfo info)
{
    wstringstream wss;
    wss << info.vendor_id
        << L":" << info.product_id
        << L":" << info.serial;
    return wss.str();
}

MTPDeviceIdInfo fromIDStr(wstring str)
{
    MTPDeviceIdInfo info;

    wstring tmp;
    wchar_t *pEnd;
    wstringstream wss(str);

    std::getline(wss, tmp, L':');
    info.vendor_id = wcstol(tmp.c_str(), &pEnd, 10);
    std::getline(wss, tmp, L':');
    info.product_id = wcstol(tmp.c_str(), &pEnd, 10);
    std::getline(wss, tmp, L':');
    info.serial = tmp.c_str();

    return info;
}

MTPDeviceInfo toMTPDeviceInfo(LIBMTP_mtpdevice_t *device, uint16_t vendorId, uint16_t productId)
{
    char *serial = LIBMTP_Get_Serialnumber(device);
    char *friendly = LIBMTP_Get_Friendlyname(device);
    char *description = LIBMTP_Get_Modelname(device);
    char *manufacturer = LIBMTP_Get_Manufacturername(device);
    MTPDeviceIdInfo idInfo;
    idInfo.vendor_id = vendorId;
    idInfo.product_id = productId;
    idInfo.serial = charToWstring(serial);

    MTPDeviceInfo deviceInfo;

    deviceInfo.deviceId = toIDStr(idInfo);
    deviceInfo.friendlyName = charToWstring(friendly);
    deviceInfo.description = charToWstring(description);
    deviceInfo.manufacturer = charToWstring(manufacturer);

    free(serial);
    free(friendly);
    free(description);
    free(manufacturer);

    return deviceInfo;
}

LIBMTP_mtpdevice_t *getOpenDevice(wstring id)
{
    LIBMTP_raw_device_t *raw_devs = nullptr;
    LIBMTP_mtpdevice_t *open_device = nullptr;
    LIBMTP_error_number_t ret;
    int numdevs;

    ret = LIBMTP_Detect_Raw_Devices(&raw_devs, &numdevs);

    if (ret == LIBMTP_ERROR_NONE && numdevs > 0)
    {
        for (int i = 0; i < numdevs; i++)
        {
            LIBMTP_mtpdevice_t *found_device = LIBMTP_Open_Raw_Device_Uncached(&raw_devs[i]);
            char *serial = LIBMTP_Get_Serialnumber(found_device);
            MTPDeviceIdInfo idInfo;
            idInfo.vendor_id = raw_devs[i].device_entry.vendor_id;
            idInfo.product_id = raw_devs[i].device_entry.product_id;
            idInfo.serial = charToWstring(serial);
            free(serial);

            wstring found_id = toIDStr(idInfo);
            if (found_id.compare(id.c_str()))
            {
                open_device = found_device;
                break;
            }
            else
            {
                LIBMTP_Release_Device(found_device);
            }
        }
    }

    free(raw_devs);

    return open_device;
}

vector<MTPDeviceInfo> getDevicesInfo()
{
    vector<MTPDeviceInfo> devices;
    LIBMTP_raw_device_t *raw_devs = nullptr;
    LIBMTP_error_number_t ret;
    int numdevs;

    ret = LIBMTP_Detect_Raw_Devices(&raw_devs, &numdevs);

    if (ret == LIBMTP_ERROR_NONE && numdevs > 0)
    {
        for (int i = 0; i < numdevs; i++)
        {
            LIBMTP_mtpdevice_t *device = LIBMTP_Open_Raw_Device_Uncached(&raw_devs[i]);
            MTPDeviceInfo deviceInfo = toMTPDeviceInfo(device,
                                                       raw_devs[i].device_entry.vendor_id,
                                                       raw_devs[i].device_entry.product_id);
            devices.push_back(deviceInfo);

            LIBMTP_Release_Device(device);
        }
    }
    free(raw_devs);
    return devices;
}

optional<MTPDeviceInfo> getDeviceInfo(wstring id)
{
    LIBMTP_mtpdevice_t *device = getOpenDevice(id);
    if (device != nullptr)
    {
        MTPDeviceIdInfo idInfo = fromIDStr(id);
        MTPDeviceInfo deviceInfo = toMTPDeviceInfo(device, idInfo.vendor_id, idInfo.product_id);
        LIBMTP_Release_Device(device);
        return deviceInfo;
    }
    return std::nullopt;
}

void initMTP()
{
    LIBMTP_Init();
}

vector<wstring> getChildIds(wstring deviceId, wstring parentId)
{
    vector<wstring> childIds;
    LIBMTP_mtpdevice_t *device = getOpenDevice(deviceId);
    if (device != nullptr)
    {
        wchar_t *pEnd;
        uint32_t nParentId = wcstoul(parentId.c_str(), &pEnd, 10);
        LIBMTP_file_t *children = LIBMTP_Get_Files_And_Folders(device, nParentId, 0);
        LIBMTP_Release_Device(device);
    }
    return childIds;
}

optional<MTPContentNode> createDirNode(wstring deviceId, wstring parentId, wstring name);
optional<MTPContentNode> createContentNode(wstring deviceId, wstring parentId, wstring file);
optional<MTPContentNode> readNode(wstring deviceId, wstring id);
optional<MTPContentNode> copyNode(wstring deviceId, wstring parentId, wstring id, wstring tmpFolder);
bool deleteNode(wstring deviceId, wstring id);
bool retrieveNode(wstring deviceId, wstring id, wstring destFolder);
} // namespace LibJMTP

int main()
{
    LibJMTP::initMTP();
    wstringstream wss;
    wss << LIBMTP_FILES_AND_FOLDERS_ROOT;
    auto childIds = LibJMTP::getChildIds(L"16642:4497:F2000018D562F2A412B4", wss.str());
    wcout << endl;
}