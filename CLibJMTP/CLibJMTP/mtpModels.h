#pragma once
#include <string>
#include <vector>

using std::wstring;
using std::vector;

class MTPDevice {
private:
	wstring id;
	wstring desc;
	wstring fName;
	wstring manu;

	void init(const wchar_t* dId, const wchar_t* dDesc, const wchar_t* dFName, const wchar_t* dManu);

public:
	MTPDevice() {};
	MTPDevice(PWSTR dId, PWSTR dDesc, PWSTR dFName, PWSTR dManu);
	MTPDevice(const MTPDevice &other);
	~MTPDevice();

	const wstring  getId();
	void setId(PWSTR newId);
	const wstring  getDescription();
	void setDescription(PWSTR newDesc);
	const wstring  getFriendlyName();
	void setFriendlyName(PWSTR newFName);
	const wstring  getManufacturer();
	void setManufacturer(PWSTR newManu);

	MTPDevice& operator=(const MTPDevice &other);

	wstring toString();
};

class MTPObjectTree {
private:
	wstring id;
	wstring parentId;
	wstring persistId;
	wstring name;
	wstring origName;
	ULONGLONG size;
	ULONGLONG capacity;

	void init(const wchar_t* id, const wchar_t* parentId, const wchar_t* persistId, const wchar_t* name, const wchar_t* origName, ULONGLONG size, ULONGLONG capacity, vector<MTPObjectTree*> children);
	wstring toPrettyString(unsigned int level);

public:
	vector<MTPObjectTree*> children;

	MTPObjectTree() {};
	MTPObjectTree(PCWSTR id);
	MTPObjectTree(const MTPObjectTree &other);
	~MTPObjectTree();

	const wstring getId();
	void setId(PWSTR id);
	const wstring getParentId();
	void setParentId(PWSTR parentId);
	const wstring getPersistId();
	void setPersistId(PWSTR persistId);
	const wstring getName();
	void setName(PWSTR name);
	const wstring getOrigName();
	void setOrigName(PWSTR origName);

	ULONGLONG getSize();
	void setSize(ULONGLONG size);
	ULONGLONG getCapacity();
	void setCapacity(ULONGLONG capacity);

	MTPObjectTree& operator=(const MTPObjectTree &other);

	wstring toString();
	wstring toPrettyString();
};