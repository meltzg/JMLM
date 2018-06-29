#include <string>
#include "commonHelpers.h"

namespace LibJMTP {
	void wcsAllocCpy(wchar_t ** destination, const wchar_t * source)
	{
		delete[](*destination);
		*destination = nullptr;
		if (source != nullptr) {
			size_t size = wcslen(source) + 1;
			(*destination) = new wchar_t[size];
			wcscpy_s(*destination, size, source);
		}
	}
}