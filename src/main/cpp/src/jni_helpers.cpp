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

jobject ulonglongToJBigInt(JNIEnv *env, unsigned long long num)
{
    jclass big_int_class = env->FindClass(JBIGINT);

    ostringstream sig;
    sig << "("
        << JSTRING
        << ")V";

    jmethodID big_int_constructor = env->GetMethodID(big_int_class, JCONSTRUCTOR, sig.str().c_str());

    // the number needs to be a string for BigInteger's constructor
    wstring str_num = to_wstring(num);
    jstring jstr_num = wcharToJString(env, str_num.c_str());

    return env->NewObject(big_int_class, big_int_constructor, jstr_num);
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
        << JMTPDEVICE
        << JSTRING
        << JSTRING
        << JSTRING
        << JSTRING
        << ")V";

    jmethodID device_info_constr = env->GetMethodID(device_info_class, JCONSTRUCTOR, sig.str().c_str());

    jstring jdevice_id = wcharToJString(env, info.device_id.c_str());
    jstring jdescription = wcharToJString(env, info.description.c_str());
    jstring jfriendly_name = wcharToJString(env, info.friendly_name.c_str());
    jstring jmanufacturer = wcharToJString(env, info.manufacturer.c_str());

    jobject jinfo = env->NewObject(device_info_class,
                                   device_info_constr,
                                   obj,
                                   jdevice_id,
                                   jfriendly_name,
                                   jdescription,
                                   jmanufacturer);

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

jobject toJMTPContentNode(JNIEnv *env, MTPContentNode node)
{
    jclass content_node_class = env->FindClass(JMTPCONTENTNODE);

    ostringstream sig;
    sig << "("
        << JSTRING
        << JSTRING
        << JSTRING
        << JSTRING
        << "Z"
        << JBIGINT
        << JBIGINT
        << ")V";

    jmethodID content_node_constr = env->GetMethodID(content_node_class, JCONSTRUCTOR, sig.str().c_str());

    jstring id = wcharToJString(env, node.id.c_str());
    jstring parent_id = wcharToJString(env, node.parent_id.c_str());
    jstring name = wcharToJString(env, node.name.c_str());
    jstring orig_name = wcharToJString(env, node.orig_name.c_str());
    jobject size = ulonglongToJBigInt(env, node.size);
    jobject capacity = ulonglongToJBigInt(env, node.capacity);

    jobject jnode = env->NewObject(content_node_class,
                                   content_node_constr,
                                   id,
                                   parent_id,
                                   name,
                                   orig_name,
                                   node.is_directory,
                                   size,
                                   capacity);

    return jnode;
}
jobject toJMTPStorageDevice(JNIEnv *env, MTPStorageDevice storage_device);
} // namespace jmtp
