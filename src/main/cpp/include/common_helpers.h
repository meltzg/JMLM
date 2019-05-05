#pragma once
#include <string>

namespace jmtp
{
void wcsAllocCpy(wchar_t **destination, const wchar_t *source);
std::wstring charToWString(char *str);
} // namespace jmtp