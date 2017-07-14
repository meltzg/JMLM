#include <PortableDeviceApi.h>
#include <sstream>
#include "mtpModels.h"

using std::wstringstream;

void wcsAllocCpy(wchar_t **destination, wchar_t* source) {
	delete[] (*destination);
	size_t size = wcslen(source) << 1;
	(*destination) = new wchar_t[size];
	wcscpy_s(*destination, size, source);
}

void MTPDevice::init(PWSTR dId, PWSTR dDesc, PWSTR dFName, PWSTR dManu)
{
	id = desc = fName = manu = NULL;
	wcsAllocCpy(&id, dId);
	wcsAllocCpy(&desc, dDesc);
	wcsAllocCpy(&fName, dFName);
	wcsAllocCpy(&manu, dManu);
}

MTPDevice::MTPDevice(PWSTR dId, PWSTR dDesc, PWSTR dFName, PWSTR dManu)
{
	init(dId, dDesc, dFName, dManu);
}

MTPDevice::MTPDevice(const MTPDevice & other)
{
	init(other.id, other.desc, other.fName, other.manu);
}

MTPDevice::~MTPDevice()
{
	delete[] id;
	delete[] desc;
	delete[] fName;
	delete[] manu;
}

PWSTR MTPDevice::getId()
{
	return id;
}

void MTPDevice::setId(PWSTR newId)
{
	id = newId;
}

PWSTR MTPDevice::getDescription()
{
	return desc;
}

void MTPDevice::setDescription(PWSTR newDesc)
{
	desc = newDesc;
}

PWSTR MTPDevice::getFriendlyName()
{
	return fName;
}

void MTPDevice::setFriendlyName(PWSTR newFName)
{
	fName = newFName;
}

PWSTR MTPDevice::getManufacturer()
{
	return manu;
}

void MTPDevice::setManufacturer(PWSTR newManu)
{
	manu = newManu;
}

wstring MTPDevice::toString() {
	wstringstream wstr; 
	wstr << L"MTPDevice [deviceId=" << id << L", friendlyName=" << fName << L", description=" << desc
		<< L", manufacturer=" << manu << L"]";

	return wstr.str();
}
