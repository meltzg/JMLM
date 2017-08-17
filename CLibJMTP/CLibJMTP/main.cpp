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

			wstring newObjId = transferToDevice(L"D:/Users/vader/Desktop/test space.mp3", L"o2", L"this/is/a/test.mp3");
			wcout << "Transfer to device successful: " << newObjId << endl;

			//bool transferSuccess = transferFromDevice(newObjId.c_str(), L"D:/Users/vader/Desktop/test/transfer.mp3");
			//wcout << "transfer from device successful: " << transferSuccess << endl;

			bool deleteSuccess = removeFromDevice(newObjId.c_str(), L"o2");
			wcout << "Delete successful: " << deleteSuccess << endl;
		}
	}
}