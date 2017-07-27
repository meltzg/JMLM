#include <string>
#include "commonHelpers.h"

void wcsAllocCpy(wchar_t **destination, wchar_t* source) {
	delete[](*destination);
	*destination = nullptr;
	if (source != nullptr) {
		size_t size = wcslen(source) + 1;
		(*destination) = new wchar_t[size];
		wcscpy_s(*destination, size, source);
	}
}

bool cmp_wstr::operator()(const wchar_t * a, const wchar_t * b) const
{
	int cmp = std::wcscmp(a, b);

	if (cmp < 0) {
		return true;
	}
	return false;
	//return std::wcscmp(a, b) == 0;
}