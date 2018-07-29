#pragma once
#include <string>

namespace LibJMTP
{
void wcsAllocCpy(wchar_t **destination, const wchar_t *source);
std::wstring charToWstring(char *str);
} // namespace LibJMTP