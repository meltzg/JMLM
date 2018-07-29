#include <libmtp.h>
#include "mtpHelpers.h"

#include <iostream>

using std::cout;
using std::endl;

int main() {
    LIBMTP_raw_device_t *devices;
    LIBMTP_error_number_t ret;
    int numdevs;

    ret = LIBMTP_Detect_Raw_Devices(&devices, &numdevs);
    cout << "Device Count: " << numdevs << endl;
    cout << "Bus Location: " << devices[0].bus_location << endl;
}