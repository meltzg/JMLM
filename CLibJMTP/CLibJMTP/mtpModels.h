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

	wstring toString();
};

class MTPObject {
private:
	PWSTR id;
	vector<MTPObject> children;

	void init(PWSTR id);

public:
	MTPObject() : id(NULL) {};
	MTPObject(PWSTR id);
	MTPObject(const MTPObject &other);
	~MTPObject();

	const PWSTR getId();
	void setId(PWSTR id);

	wstring toString();
};