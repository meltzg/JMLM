// #include <wchar.h>
#include <stdio.h>
// #include <string.h>
#include "jni_helpers.h"

// jstring wcharToJString(JNIEnv *env, const wchar_t *wstr)
// {
//     mbstate_t state;
//     memset(&state, 0, sizeof state);
//     size_t len = 1 + wcsrtombs(NULL, &wstr, 0, &state);
//     char mbstr[len];
//     wcsrtombs(mbstr, &wstr, len, &state);
//     jstring ret = env->NewStringUTF(mbstr);
//     return ret;
// }

// wstring jStringToWString(JNIEnv *env, jstring jstr)
// {
//     if (jstr == NULL)
//     {
//         return nullptr;
//     }

//     const jchar *raw = env->GetStringChars(jstr, 0);
//     jsize len = env->GetStringLength(jstr);
//     wstring wstr;
//     wchar_t *wstr_c = new wchar_t[len + 1];

//     wstr.assign(raw, raw + len);
//     wcscpy(wstr_c, wstr.c_str());

//     wstring ret(wstr_c);
//     delete[] wstr_c;

//     return ret;
// }

jobject getNewArrayList(JNIEnv *env)
{
    jclass array_list_class = (*env)->FindClass(env, JARRLIST);
    jmethodID array_list_constructor = (*env)->GetMethodID(env, array_list_class, "<init>", "()V");
    return (*env)->NewObject(env, array_list_class, array_list_constructor);
}

void arrayListAdd(JNIEnv *env, jobject list, jobject element)
{
    jclass array_list_class = (*env)->FindClass(env, JARRLIST);
    jmethodID array_list_add = (*env)->GetMethodID(env, array_list_class, "add", "(Ljava/lang/Object;)Z");
    jboolean val = (*env)->CallBooleanMethod(env, list, array_list_add, element);
}

jobject toJMTPDeviceInfo(JNIEnv *env, jobject obj, MTPDeviceInfo info)
{
    jclass device_info_class = (*env)->FindClass(env, JMTPDEVICEINFO);
    char sig[1024];
    sprintf(sig, "(%s%s%s%s%sJJ)V", JSTRING, JSTRING, JSTRING, JSTRING, JSTRING);
    jmethodID device_info_constr = (*env)->GetMethodID(env, device_info_class, JCONSTRUCTOR, sig);
    jstring jdevice_id = (*env)->NewStringUTF(env, info.device_id);
    jstring jdescription = (*env)->NewStringUTF(env, info.description);
    jstring jfriendly_name = (*env)->NewStringUTF(env, info.friendly_name);
    jstring jmanufacturer = (*env)->NewStringUTF(env, info.manufacturer);
    jstring jserial = (*env)->NewStringUTF(env, info.id_info.serial);

    jobject jinfo = (*env)->NewObject(env,
                                      device_info_class,
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

jobject toJMTPDeviceInfoList(JNIEnv *env, jobject obj, MTPDeviceInfo *info, int numDevices)
{
    jobject jlist = getNewArrayList(env);
    for (int i = 0; i < numDevices; i++)
    {
        jobject jinfo = toJMTPDeviceInfo(env, obj, info[i]);
        arrayListAdd(env, jlist, jinfo);
    }
    return jlist;
}

jobject toJMTPStorageDevice(JNIEnv *env, MTPStorageDevice storage_device)
{
    jclass storage_info_class = (*env)->FindClass(env, JMTPSTORAGEDEVICE);
    char sig[1024];
    sprintf(sig, "(%sJJI)V", JSTRING);
    jmethodID storage_info_constr = (*env)->GetMethodID(env, storage_info_class, JCONSTRUCTOR, sig);
    jstring jstorage_id = (*env)->NewStringUTF(env, storage_device.storage_id);

    jobject jstorage = (*env)->NewObject(env,
                                         storage_info_class,
                                         storage_info_constr,
                                         jstorage_id,
                                         storage_device.capacity,
                                         storage_device.free_space,
                                         0);
    return jstorage;
}
