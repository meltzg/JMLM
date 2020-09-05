#ifndef MTP_HELPERS_H
#define MTP_HELPERS_H

#include <stdbool.h>
#include <stdint.h>
#include "mtp_models.h"

static const unsigned int NUM_OBJECTS_TO_REQUEST = 100;

char *toDeviceId(uint32_t bus_location, uint8_t devnum, uint16_t product_id, uint16_t vendor_id);

int getDevicesInfo(MTPDeviceInfo_t **devices);
bool getDeviceInfo(MTPDeviceInfo_t *deviceInfo, const char *id);
void initMTP();

char *getStorageDeviceId(const char *device_id, const char *path);
bool getStorageDeviceMetadata(MTPStorageDevice_t *storageDevice, const char *device_id, const char *storage_id);

uint8_t *getFileContent(const char *device_id, const char *path, uint64_t *size);
int writeFileContent(const char *device_id, const char *path, const char *content, long offset, int size);
bool deletePath(const char *device_id, const char *path);

bool isDirectory(const char *device_id, const char *path);
long fileSize(const char *device_id, const char *path);
char **getPathChildren(const char *device_id, const char *path, int *numChildren);

#endif
