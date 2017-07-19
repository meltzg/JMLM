#include <PortableDeviceApi.h>
#include <sstream>
#include "mtpModels.h"

using std::wstringstream;

void wcsAllocCpy(wchar_t **destination, wchar_t* source) {
	delete[] (*destination);
	*destination = nullptr;
	if (source != nullptr) {
		size_t size = wcslen(source) + 1;
		(*destination) = new wchar_t[size];
		wcscpy_s(*destination, size, source);
	}
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

const PWSTR MTPDevice::getId()
{
	return id;
}

void MTPDevice::setId(PWSTR newId)
{
	wcsAllocCpy(&id, newId);
}

const PWSTR MTPDevice::getDescription()
{
	return desc;
}

void MTPDevice::setDescription(PWSTR newDesc)
{
	wcsAllocCpy(&desc, newDesc);
}

const PWSTR MTPDevice::getFriendlyName()
{
	return fName;
}

void MTPDevice::setFriendlyName(PWSTR newFName)
{
	wcsAllocCpy(&fName, newFName);
}

const PWSTR MTPDevice::getManufacturer()
{
	return manu;
}

void MTPDevice::setManufacturer(PWSTR newManu)
{
	wcsAllocCpy(&manu, newManu);
}

MTPDevice & MTPDevice::operator=(const MTPDevice & other)
{
	init(other.id, other.desc, other.fName, other.manu);
	return *this;
}

wstring MTPDevice::toString() {
	wstringstream wstr; 
	wstr << L"MTPDevice [deviceId=" << id << L", friendlyName=" << fName << L", description=" << desc
		<< L", manufacturer=" << manu << L"]";

	return wstr.str();
}

void MTPObjectTree::init(PWSTR id, PWSTR parentId, PWSTR name, PWSTR origName, ULONGLONG size, vector<MTPObjectTree> children)
{
	this->id = this->parentId = this->name = this->origName = NULL;
	this->size = size;
	wcsAllocCpy(&(this->id), id);
	wcsAllocCpy(&(this->parentId), parentId);
	wcsAllocCpy(&(this->name), name);
	wcsAllocCpy(&(this->origName), origName);
	this->children.clear();
	for (auto child : children) {
		this->children.push_back(child);
	}
}

MTPObjectTree::MTPObjectTree(PWSTR id)
{
	init(id, nullptr, nullptr, nullptr, 0, vector<MTPObjectTree>());
}

MTPObjectTree::MTPObjectTree(const MTPObjectTree & other)
{
	init(other.id, other.parentId, other.name, other.origName, other.size, other.children);
}

MTPObjectTree::~MTPObjectTree()
{
	delete[] id;
	delete[] parentId;
	delete[] name;
	delete[] origName;
}

const PWSTR MTPObjectTree::getId()
{
	return id;
}

void MTPObjectTree::setId(PWSTR id)
{
	wcsAllocCpy(&(this->id), id);
}

const PWSTR MTPObjectTree::getParentId()
{
	return parentId;
}

void MTPObjectTree::setParentId(PWSTR parentId)
{
	wcsAllocCpy(&(this->parentId), parentId);
}

const PWSTR MTPObjectTree::getName()
{
	return name;
}

void MTPObjectTree::setName(PWSTR name)
{
	wcsAllocCpy(&(this->name), name);
}

const PWSTR MTPObjectTree::getOrigName()
{
	return origName;
}

void MTPObjectTree::setOrigName(PWSTR origName)
{
	wcsAllocCpy(&(this->origName), origName);
}

ULONGLONG MTPObjectTree::getSize()
{
	return size;
}

void MTPObjectTree::setSize(ULONGLONG size)
{
	this->size = size;
}

MTPObjectTree & MTPObjectTree::operator=(const MTPObjectTree & other)
{
	init(other.id, other.parentId, other.name, other.origName, other.size, other.children);
	return *this;
}

wstring MTPObjectTree::toString()
{
	wstringstream wstr;
	wstr << L"MTPObjectTree [id=" << (id != nullptr ? id : L"NULL")
		<< L", parentId=" << (parentId != nullptr ? parentId : L"NULL")
		<< L", name=" << (name != nullptr ? name : L"NULL")
		<< L", origName=" << (origName != nullptr ? origName : L"NULL")
		<< L", size=" << size
		<< L", children=[";
	for (auto child : children) {
		wstr << child.toString() << ", ";
	}
	wstr << "]]";

	return wstr.str();
}
