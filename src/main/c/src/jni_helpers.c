#include <stdio.h>
#include "jni_helpers.h"

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

jobject toJMTPDeviceInfo(JNIEnv *env, jobject obj, MTPDeviceInfo_t info)
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

jobject toJMTPDeviceInfoList(JNIEnv *env, jobject obj, MTPDeviceInfo_t *info, int numDevices)
{
    jobject jlist = getNewArrayList(env);
    for (int i = 0; i < numDevices; i++)
    {
        jobject jinfo = toJMTPDeviceInfo(env, obj, info[i]);
        arrayListAdd(env, jlist, jinfo);
    }
    return jlist;
}

jobject toJMTPStorageDevice(JNIEnv *env, MTPStorageDevice_t storage_device)
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

jint throwIOException(JNIEnv *env, const char *message)
{
    jclass ioexception_class = (*env)->FindClass(env, JIOEXCEPTION);
    return (*env)->ThrowNew(env, ioexception_class, message);
}
