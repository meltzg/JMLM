#pragma once

void wcsAllocCpy(wchar_t **destination, wchar_t* source);

struct cmp_wstr {
	bool operator()(wchar_t const *a, wchar_t const *b) const;
};