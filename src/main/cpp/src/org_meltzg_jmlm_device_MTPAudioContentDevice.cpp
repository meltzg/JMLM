#include <iostream>
#include "org_meltzg_jmlm_device_MTPAudioContentDevice.h"
#include "mtp_helpers.h"
#include "common_helpers.h"

using std::endl;
using std::hex;
using std::wcout;

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPAudioContentDevice_getStorageDevice(JNIEnv *env, jobject obj, jstring path, jstring deviceId)
{
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPAudioContentDevice_getDevicesInfo(JNIEnv *env, jobject cls)
{
}

JNIEXPORT jobject JNICALL Java_org_meltzg_jmlm_device_MTPAudioContentDevice_getDeviceInfo(JNIEnv *env, jobject cls, jstring deviceId)
{
}

// Available devices (busLocation, devNum, productId, vendorId, product, vendor):
// 1, 2, 0x1191, 0x4102, UNKNOWN, UNKNOWN
int main(int argc, char *argv)
{
  auto devices = jmtp::getDevicesInfo();
  for (int i = 0; i < devices.size(); i++)
  {
    auto device = devices[i];
    wcout << device.busLocation << ' ' <<
      device.devNum << ' ' <<
      device.device_id << ' ' <<
      device.friendly_name << ' ' <<
      device.description << ' ' <<
      device.manufacturer << ' ' <<
      hex << device.id_info.product_id << ' ' <<
      hex << device.id_info.vendor_id << ' ' <<
      device.id_info.serial << endl;
  }
}