#pragma once
#include "mtp_models.h"

const unsigned int NUM_OBJECTS_TO_REQUEST = 100;

char *toDeviceId(uint32_t bus_location, uint8_t devnum, uint16_t product_id, uint16_t vendor_id);

MTPDeviceInfo *getDevicesInfo();
MTPDeviceInfo getDeviceInfo(char *id);
void initMTP();

MTPStorageDevice getStorageDevice(char *device_id, char *path);
