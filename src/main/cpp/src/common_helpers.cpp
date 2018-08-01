#include <cstring>
#include "commonHelpers.h"

using std::wstring;

namespace LibJMTP
{
void wcsAllocCpy(wchar_t **destination, const wchar_t *source)
{
}
wstring charToWstring(char *str)
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
} // namespace LibJMTP