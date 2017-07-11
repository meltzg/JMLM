#pragma once

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

	PWSTR  getId();
	void setId(PWSTR newId);
	PWSTR  getDescription();
	void setDescription(PWSTR newDesc);
	PWSTR  getFriendlyName();
	void setFriendlyName(PWSTR newFName);
	PWSTR  getManufacturer();
	void setManufacturer(PWSTR newManu);
};