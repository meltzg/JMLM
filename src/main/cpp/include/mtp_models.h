#pragma once
#include <string>

namespace jmtp
{

struct MTPContentNode
{
	std::wstring id;
	std::wstring parent_id;
	std::wstring orig_name;
	std::wstring name;
	bool is_directory;
	unsigned long long size;
	unsigned long long capacity;

	bool is_valid;

	MTPContentNode() { is_valid = false; }
};

struct MTPDeviceInfo
{
	std::wstring device_id;
	std::wstring friendly_name;
	std::wstring description;
	std::wstring manufacturer;
};
} // namespace jmtp
