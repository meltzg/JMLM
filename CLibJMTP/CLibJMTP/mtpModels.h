#pragma once
#include <string>
#include <vector>

using std::wstring;
using std::vector;

void wcsAllocCpy(wchar_t **destination, wchar_t* source);

class MTPDevice {
private:
	PWSTR id;
	PWSTR desc;
	PWSTR fName;
	PWSTR manu;

	void init(PWSTR dId, PWSTR dDesc, PWSTR dFName, PWSTR dManu);

public:
	MTPDevice() : id(NULL), desc(NULL), fName(NULL), manu(NULL) {};
	MTPDevice(PWSTR dId, PWSTR dDesc, PWSTR dFName, PWSTR dManu);
	MTPDevice(const MTPDevice &other);
	~MTPDevice();

	const PWSTR  getId();
	void setId(PWSTR newId);
	const PWSTR  getDescription();
	void setDescription(PWSTR newDesc);
	const PWSTR  getFriendlyName();
	void setFriendlyName(PWSTR newFName);
	const PWSTR  getManufacturer();
	void setManufacturer(PWSTR newManu);

	MTPDevice& operator=(const MTPDevice &other);

	wstring toString();
};

class MTPObjectTree {
private:
	PWSTR id;
	PWSTR parentId;
	PWSTR name;
	PWSTR origName;
	ULONGLONG size;

	void init(PWSTR id, PWSTR parentId, PWSTR name, PWSTR origName, ULONGLONG size, vector<MTPObjectTree> children);

public:
	vector<MTPObjectTree> children;

	MTPObjectTree() : id(nullptr), parentId(nullptr), name(nullptr), origName(nullptr), size(0) {};
	MTPObjectTree(PWSTR id);
	MTPObjectTree(const MTPObjectTree &other);
	~MTPObjectTree();

	const PWSTR getId();
	void setId(PWSTR id);
	const PWSTR getParentId();
	void setParentId(PWSTR parentId);
	const PWSTR getName();
	void setName(PWSTR name);
	const PWSTR getOrigName();
	void setOrigName(PWSTR origName);

	ULONGLONG getSize();
	void setSize(ULONGLONG size);

	MTPObjectTree& operator=(const MTPObjectTree &other);

	wstring toString();
};