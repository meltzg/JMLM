#include <cstring>
#include "common_helpers.h"

using std::wstring;

namespace jmtp
{
void wcsAllocCpy(wchar_t **destination, const wchar_t *source)
{
}
wstring charToWString(char *str)
{
    if (str != nullptr)
    {
        const size_t len = strlen(str) + 1;
        wstring wstr(len, L'#');
        mbstowcs(&wstr[0], str, len);
        return wstr;
    }
    return wstring();
}
} // namespace jmtp
