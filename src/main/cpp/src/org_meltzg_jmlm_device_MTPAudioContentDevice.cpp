#include <iostream>
#include "org_meltzg_jmlm_device_MTPAudioContentDevice.h"
#include "mtp_helpers.h"

using std::endl;
using std::hex;
using std::wcout;

JNIEXPORT jobject JNICALL
Java_org_meltzg_jmlm_device_MTPAudioContentDevice_getStorageDevice(JNIEnv *env, jobject obj, jstring path, jint productId, jint vendorId, jint busLocation, jint devNum)
{
  return nullptr;
}

// Available devices (busLocation, devNum, productId, vendorId, product, vendor):
// 1, 2, 0x1191, 0x4102, UNKNOWN, UNKNOWN
int main(int argc, char *argv)
{
  auto devices = jmtp::getDevicesInfo();
  for (int i = 0; i < devices.size(); i++)
  {
    wcout << hex << devices[i].id_info.product_id << ' ' << hex << devices[i].id_info.vendor_id << endl;

    std::optional<std::wstring> id = jmtp::toDeviceId(1, 2, devices[i].id_info.product_id, devices[i].id_info.vendor_id);
    if (id.has_value())
    {
      wcout << "id string:" << id.value() << endl;
    }
  }
}