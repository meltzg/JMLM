#include <libmtp.h>
#include <sstream>
#include <cwchar>
#include "mtp_helpers.h"
#include "common_helpers.h"

#include <iostream>

using std::endl;
using std::optional;
using std::vector;
using std::wcout;
using std::wstring;
using std::wstringstream;

namespace jmtp
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

MTPDeviceInfo toMTPDeviceInfo(LIBMTP_mtpdevice_t *device, uint16_t vendor_id, uint16_t product_id)
{
    char *serial = LIBMTP_Get_Serialnumber(device);
    char *friendly = LIBMTP_Get_Friendlyname(device);
    char *description = LIBMTP_Get_Modelname(device);
    char *manufacturer = LIBMTP_Get_Manufacturername(device);
    MTPDeviceIdInfo id_info;
    id_info.vendor_id = vendor_id;
    id_info.product_id = product_id;
    id_info.serial = charToWString(serial);

    MTPDeviceInfo device_info;

    device_info.device_id = toIDStr(id_info);
    device_info.friendly_name = charToWString(friendly);
    device_info.description = charToWString(description);
    device_info.manufacturer = charToWString(manufacturer);

    free(serial);
    free(friendly);
    free(description);
    free(manufacturer);

    return device_info;
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
            MTPDeviceIdInfo id_info;
            id_info.vendor_id = raw_devs[i].device_entry.vendor_id;
            id_info.product_id = raw_devs[i].device_entry.product_id;
            id_info.serial = charToWString(serial);
            free(serial);

            wstring found_id = toIDStr(id_info);
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
            MTPDeviceInfo device_info = toMTPDeviceInfo(device,
                                                        raw_devs[i].device_entry.vendor_id,
                                                        raw_devs[i].device_entry.product_id);
            devices.push_back(device_info);

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
        MTPDeviceIdInfo id_info = fromIDStr(id);
        MTPDeviceInfo device_info = toMTPDeviceInfo(device, id_info.vendor_id, id_info.product_id);
        LIBMTP_Release_Device(device);
        return device_info;
    }
    return std::nullopt;
}

void initMTP()
{
    LIBMTP_Init();
}

vector<wstring> getChildIds(wstring device_id, wstring parent_id)
{
    vector<wstring> child_ids;
    LIBMTP_mtpdevice_t *device = getOpenDevice(device_id);
    if (device != nullptr)
    {
        wchar_t *pEnd;
        uint32_t nParentId = wcstoul(parent_id.c_str(), &pEnd, 10);
        LIBMTP_file_t *children = LIBMTP_Get_Files_And_Folders(device, nParentId, 0);
        LIBMTP_Release_Device(device);
    }
    return child_ids;
}

optional<MTPContentNode> createDirNode(wstring device_id, wstring parent_id, wstring name);
optional<MTPContentNode> createContentNode(wstring device_id, wstring parent_id, wstring file);
optional<MTPContentNode> readNode(wstring device_id, wstring id);
optional<MTPContentNode> copyNode(wstring device_id, wstring parent_id, wstring id, wstring tmp_folder);
bool deleteNode(wstring device_id, wstring id);
bool retrieveNode(wstring device_id, wstring id, wstring dest_folder);
optional<MTPStorageDevice> getStorageDevice(wstring device_id, wstring id);
} // namespace jmtp

int main()
{
    jmtp::initMTP();
    wstringstream wss;
    wss << LIBMTP_FILES_AND_FOLDERS_ROOT;
    auto child_ids = jmtp::getChildIds(L"16642:4497:F2000018D562F2A412B4", wss.str());
    wcout << endl;
}
