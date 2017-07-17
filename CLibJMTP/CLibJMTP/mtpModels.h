#pragma once
#include <string>
#include <vector>

using std::wstring;
using std::vector;

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

	void init(PWSTR id, vector<MTPObjectTree> children);

public:
	vector<MTPObjectTree> children;

	MTPObjectTree() : id(nullptr) {};
	MTPObjectTree(PWSTR id);
	MTPObjectTree(const MTPObjectTree &other);
	~MTPObjectTree();

	const PWSTR getId();
	void setId(PWSTR id);

	MTPObjectTree& operator=(const MTPObjectTree &other);

	wstring toString();
};