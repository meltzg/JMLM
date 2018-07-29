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
            LIBMTP_mtpdevice_t *device = LIBMTP_Open_Raw_Device(&raw_devs[i]);
            char *serial = LIBMTP_Get_Serialnumber(device);
            char *friendly = LIBMTP_Get_Friendlyname(device);
            char *description = LIBMTP_Get_Modelname(device);
            char *manufacturer = LIBMTP_Get_Manufacturername(device);
            MTPDeviceIdInfo idInfo;
            idInfo.vendor_id = raw_devs[i].device_entry.vendor_id;
            idInfo.product_id = raw_devs[i].device_entry.product_id;
            idInfo.serial = charToWstring(serial);
            devices.push_back(MTPDeviceInfo());

            devices[i].deviceId = toIDStr(idInfo);
            devices[i].friendlyName = charToWstring(friendly);
            devices[i].description = charToWstring(description);
            devices[i].manufacturer = charToWstring(manufacturer);

            LIBMTP_Release_Device(device);
            free(serial);
            free(friendly);
            free(description);
            free(manufacturer);
        }
    }
    free(raw_devs);
    return devices;
}

optional<MTPDeviceInfo> getDeviceInfo(wstring id)
{
    vector<MTPDeviceInfo> devices = getDevicesInfo();
    for (MTPDeviceInfo device : devices)
    {
        if (id.compare(device.deviceId) == 0)
        {
            return device;
        }
    }
    return std::nullopt;
}

void initMTP()
{
    LIBMTP_Init();
}

vector<wstring> getChildIds(wstring deviceId, wstring parentId);
MTPContentNode createDirNode(wstring deviceId, wstring parentId, wstring name);
MTPContentNode createContentNode(wstring deviceId, wstring parentId, wstring file);
MTPContentNode readNode(wstring deviceId, wstring id);
MTPContentNode copyNode(wstring deviceId, wstring parentId, wstring id, wstring tmpFolder);
bool deleteNode(wstring deviceId, wstring id);
bool retrieveNode(wstring deviceId, wstring id, wstring destFolder);
} // namespace LibJMTP

int main()
{
    LibJMTP::initMTP();
    auto devices = LibJMTP::getDevicesInfo();
    for (auto device : devices)
    {
        wcout << L"ID: " << device.deviceId
              << L" Friendly Name: " << device.friendlyName
              << L" Description: " << device.description
              << L" Manufacturer: " << device.manufacturer << endl;
    }
    wcout << endl;
}