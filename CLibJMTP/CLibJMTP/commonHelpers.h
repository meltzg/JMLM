#pragma once

void wcsAllocCpy(wchar_t **destination, const wchar_t* source);

const wchar_t* pathDelimiter();

struct cmp_wstr {
	bool operator()(wchar_t const *a, wchar_t const *b) const;
};