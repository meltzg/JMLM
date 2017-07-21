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
			auto content = getDeviceContent();
			wcout << content->toString() << endl;
			delete content;
		}
	}

	/*wchar_t *doop = new wchar_t[5];
	wcscpy_s(doop, 5, L"doop");
	wcout << doop << endl;
	wstring doop2(doop);
	wcout << doop2 << endl;
	delete[] doop;
	doop = nullptr;
	wcout << doop2 << endl;

	wstring doop3;
	wcout << doop3 << endl << "asdf" << endl;*/
}