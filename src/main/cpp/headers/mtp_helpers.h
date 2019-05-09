#ifndef MTP_HELPERS_H
#define MTP_HELPERS_H

#include "mtp_models.h"

const unsigned int NUM_OBJECTS_TO_REQUEST = 100;

char *toDeviceId(uint32_t bus_location, uint8_t devnum, uint16_t product_id, uint16_t vendor_id);

struct MTPDeviceInfo *getDevicesInfo();
struct MTPDeviceInfo getDeviceInfo(char *id);
void initMTP();

struct MTPStorageDevice getStorageDevice(char *device_id, char *path);

#endif
