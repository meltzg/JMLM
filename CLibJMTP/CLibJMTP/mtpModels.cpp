#include <PortableDeviceApi.h>
#include <sstream>
#include "mtpModels.h"
#include "commonHelpers.h"

using std::wstringstream;

void MTPDevice::init(const wchar_t* dId, const wchar_t* dDesc, const wchar_t* dFName, const wchar_t* dManu)
{
	id.assign(dId ? dId : L"");
	desc.assign(dDesc ? dDesc : L"");
	fName.assign(dFName ? dFName : L"");
	manu.assign(dManu ? dManu : L"");
}

MTPDevice::MTPDevice(PWSTR dId, PWSTR dDesc, PWSTR dFName, PWSTR dManu)
{
	init(dId, dDesc, dFName, dManu);
}

MTPDevice::MTPDevice(const MTPDevice & other)
{
	init(other.id.c_str(), other.desc.c_str(), other.fName.c_str(), other.manu.c_str());
}

MTPDevice::~MTPDevice()
{
}

const wstring MTPDevice::getId()
{
	return id;
}

void MTPDevice::setId(PWSTR newId)
{
	id.assign(newId ? newId : L"");
}

const wstring MTPDevice::getDescription()
{
	return desc;
}

void MTPDevice::setDescription(PWSTR newDesc)
{
	desc.assign(newDesc ? newDesc : L"");
}

const wstring MTPDevice::getFriendlyName()
{
	return fName;
}

void MTPDevice::setFriendlyName(PWSTR newFName)
{
	fName.assign(newFName ? newFName : L"");
}

const wstring MTPDevice::getManufacturer()
{
	return manu;
}

void MTPDevice::setManufacturer(PWSTR newManu)
{
	manu.assign(newManu ? newManu : L"");
}

MTPDevice & MTPDevice::operator=(const MTPDevice & other)
{
	init(other.id.c_str(), other.desc.c_str(), other.fName.c_str(), other.manu.c_str());
	return *this;
}

wstring MTPDevice::toString() {
	wstringstream wstr; 
	wstr << L"MTPDevice [deviceId=" << id << L", friendlyName=" << fName << L", description=" << desc
		<< L", manufacturer=" << manu << L"]";

	return wstr.str();
}

void MTPObjectTree::init(const wchar_t* id, const wchar_t* parentId, const wchar_t* persistId, const wchar_t* name, const wchar_t* origName, ULONGLONG size, ULONGLONG capacity, vector<MTPObjectTree*> children)
{
	this->size = size;
	this->capacity = capacity;
	this->id.assign(id ? id : L"");
	this->parentId.assign(parentId ? parentId : L"");
	this->persistId.assign(persistId ? persistId : L"");
	this->name.assign(name ? name : L"");
	this->origName.assign(origName ? origName : L"");
	this->children.clear();
	for (auto child : children) {
		this->children.push_back(child);
	}
}

wstring MTPObjectTree::toPrettyString(unsigned int level)
{
	wstringstream wstr;
	for (unsigned int i = 0; i < level; i++) {
		wstr << L"\t";
	}
	
	wstr << L"MTPObjectTree [id=" << (id.length() > 0 ? id : L"NULL")
		<< L", parentId=" << (parentId.length() > 0 ? parentId : L"NULL")
		<< L", persistId=" << (persistId.length() > 0 ? persistId : L"NULL")
		<< L", name=" << (name.length() > 0 ? name : L"NULL")
		<< L", origName=" << (origName.length() > 0 ? origName : L"NULL")
		<< L", size=" << size
		<< L", capacity=" << capacity << "]" << std::endl;
	for (auto child : children) {
		wstr << child->toPrettyString(level + 1);
	}

	return wstr.str();
}

MTPObjectTree::MTPObjectTree(PCWSTR id)
{
	init(id, nullptr, nullptr, nullptr, nullptr, 0, 0, vector<MTPObjectTree*>());
}

MTPObjectTree::MTPObjectTree(const MTPObjectTree & other)
{
	init(other.id.c_str(), other.parentId.c_str(), other.persistId.c_str(), other.name.c_str(), other.origName.c_str(), other.size, other.capacity, other.children);
}

MTPObjectTree::~MTPObjectTree()
{
	for (unsigned int i = 0; i < children.size(); i++) {
		delete children[i];
		children[i] = nullptr;
	}
}

const wstring MTPObjectTree::getId()
{
	return id;
}

void MTPObjectTree::setId(PWSTR id)
{
	this->id.assign(id ? id : L"");
}

const wstring MTPObjectTree::getParentId()
{
	return parentId;
}

void MTPObjectTree::setParentId(PWSTR parentId)
{
	this->parentId.assign(parentId ? parentId : L"");
}

const wstring MTPObjectTree::getPersistId()
{
	return persistId;
}

void MTPObjectTree::setPersistId(PWSTR persistId)
{
	this->persistId.assign(persistId ? persistId : L"");
}

const wstring MTPObjectTree::getName()
{
	return name;
}

void MTPObjectTree::setName(PWSTR name)
{
	this->name.assign(name ? name : L"");
}

const wstring MTPObjectTree::getOrigName()
{
	return origName;
}

void MTPObjectTree::setOrigName(PWSTR origName)
{
	this->origName.assign(origName ? origName : L"");
}

ULONGLONG MTPObjectTree::getSize()
{
	return size;
}

void MTPObjectTree::setSize(ULONGLONG size)
{
	this->size = size;
}

ULONGLONG MTPObjectTree::getCapacity()
{
	return capacity;
}

void MTPObjectTree::setCapacity(ULONGLONG capacity)
{
	this->capacity = capacity;
}

MTPObjectTree & MTPObjectTree::operator=(const MTPObjectTree & other)
{
	init(other.id.c_str(), other.parentId.c_str(), other.persistId.c_str(), other.name.c_str(), other.origName.c_str(), other.size, other.capacity, other.children);
	return *this;
}

wstring MTPObjectTree::toString()
{
	wstringstream wstr;
	wstr << L"MTPObjectTree [id=" << (id.length() > 0 ? id : L"NULL")
		<< L", parentId=" << (parentId.length() > 0 ? parentId : L"NULL")
		<< L", persistId=" << (persistId.length() > 0 ? persistId : L"NULL")
		<< L", name=" << (name.length() > 0 ? name : L"NULL")
		<< L", origName=" << (origName.length() > 0 ? origName : L"NULL")
		<< L", size=" << size
		<< L", capacity=" << capacity
		<< L", children=[";
	for (auto child : children) {
		wstr << child->toString() << ", ";
	}
	wstr << "]]";

	return wstr.str();
}

wstring MTPObjectTree::toPrettyString()
{
	return toPrettyString(0);
}
