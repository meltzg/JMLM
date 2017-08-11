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
		auto device = getSelectedDevice(devices[0].getId().c_str());
		if (device == nullptr) {
			cerr << "Device not open\n";
		}
		else {
			/*auto content = getDeviceContent();
			wcout << content->toPrettyString() << endl;
			delete content;*/

			bool ret = transferToDevice(L"D:/Users/vader/Desktop/test space.mp3", L"o2", L"this/is/a/test.mp3");
			cout << "Transfer successful: " << ret << endl;

			ret = removeFromDevice(L"oA434", nullptr);
			cout << "Delete successful: " << ret << endl;

		}
	}
}