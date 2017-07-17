#include <iostream>
#include "mtpHelpers.h"

using namespace std;

int main() {
	initCOM();
	vector<MTPDevice> devices = getDevices();

	for (MTPDevice d : devices) {
		wcout << d.toString() << endl;
	}

	if (devices.size() > 0) {
		auto device = getSelectedDevice(devices[0].getId());
		if (device == nullptr) {
			cerr << "Device not open\n";
		}
		else {
			auto content = getDeviceContent();
			wcout << content.toString() << endl;
		}
	}
}