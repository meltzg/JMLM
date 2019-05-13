#ifndef MTP_HELPERS_H
#define MTP_HELPERS_H

#include <stdbool.h>
#include "mtp_models.h"

static const unsigned int NUM_OBJECTS_TO_REQUEST = 100;

char *toDeviceId(uint32_t bus_location, uint8_t devnum, uint16_t product_id, uint16_t vendor_id);

int getDevicesInfo(MTPDeviceInfo **devices);
bool getDeviceInfo(MTPDeviceInfo *deviceInfo, const char *id);
void initMTP();

bool getStorageDevice(MTPStorageDevice *storageDevice, const char *device_id, const char *path);

#endif
