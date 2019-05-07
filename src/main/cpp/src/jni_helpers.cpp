#include <string>
#include <sstream>
#include <wchar.h>
#include <stdio.h>
#include <string.h>
#include "jni_helpers.h"

using std::ostringstream;
using std::string;
using std::to_wstring;
using std::vector;
using std::wstring;

namespace jmtp
{
jstring wcharToJString(JNIEnv *env, const wchar_t *wstr)
{
    mbstate_t state;
    memset(&state, 0, sizeof state);
    size_t len = 1 + wcsrtombs(NULL, &wstr, 0, &state);
    char mbstr[len];
    wcsrtombs(mbstr, &wstr, len, &state);
    jstring ret = env->NewStringUTF(mbstr);
    return ret;
}

wstring jStringToWString(JNIEnv *env, jstring jstr)
{
    if (jstr == NULL)
    {
        return nullptr;
    }

    const jchar *raw = env->GetStringChars(jstr, 0);
    jsize len = env->GetStringLength(jstr);
    wstring wstr;
    wchar_t *wstr_c = new wchar_t[len + 1];

    wstr.assign(raw, raw + len);
    wcscpy(wstr_c, wstr.c_str());

    wstring ret(wstr_c);
    delete[] wstr_c;

    return ret;
}

jobject getNewArrayList(JNIEnv *env)
{
    jclass array_list_class = env->FindClass(JARRLIST);
    jmethodID array_list_constructor = env->GetMethodID(array_list_class, "<init>", "()V");
    return env->NewObject(array_list_class, array_list_constructor);
}

void arrayListAdd(JNIEnv *env, jobject list, jobject element)
{
    jclass array_list_class = env->FindClass(JARRLIST);
    jmethodID array_list_add = env->GetMethodID(array_list_class, "add", "(Ljava/lang/Object;)Z");
    env->CallBooleanMethod(list, array_list_add, element);
}

jobject toJMTPDeviceInfo(JNIEnv *env, jobject obj, MTPDeviceInfo info)
{
    jclass device_info_class = env->FindClass(JMTPDEVICEINFO);

    ostringstream sig;
    sig << "("
        << JSTRING
        << JSTRING
        << JSTRING
        << JSTRING
        << JSTRING
        << "J"
        << "J"
        << ")V";

    jmethodID device_info_constr = env->GetMethodID(device_info_class, JCONSTRUCTOR, sig.str().c_str());

    jstring jdevice_id = wcharToJString(env, info.device_id.c_str());
    jstring jdescription = wcharToJString(env, info.description.c_str());
    jstring jfriendly_name = wcharToJString(env, info.friendly_name.c_str());
    jstring jmanufacturer = wcharToJString(env, info.manufacturer.c_str());
    jstring jserial = wcharToJString(env, info.id_info.serial.c_str());

    jobject jinfo = env->NewObject(device_info_class,
                                   device_info_constr,
                                   obj,
                                   jdevice_id,
                                   jfriendly_name,
                                   jdescription,
                                   jmanufacturer,
                                   jserial,
                                   info.busLocation,
                                   info.devNum);

    return jinfo;
}

jobject toJMTPDeviceInfoList(JNIEnv *env, jobject obj, vector<MTPDeviceInfo> info)
{
    jobject jlist = getNewArrayList(env);

    for (auto iter = info.begin(); iter != info.end(); iter++)
    {
        jobject jinfo = toJMTPDeviceInfo(env, obj, *iter);

        arrayListAdd(env, jlist, jinfo);
    }
    return jlist;
}

jobject toJMTPStorageDevice(JNIEnv *env, MTPStorageDevice storage_device);
} // namespace jmtp