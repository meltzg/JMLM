#pragma once

#include <jni.h>
#include <PortableDeviceApi.h>
#include <wrl/client.h>
#include <string>

using std::string;
using Microsoft::WRL::ComPtr;

ComPtr<IPortableDeviceManager> getDeviceManager();
string formatHR(HRESULT hr);
PWSTR getDeviceDescription(PWSTR deviceId);
PWSTR getDeviceFriendlyName(PWSTR deviceId);
PWSTR getDeviceManufacturer(PWSTR deviceId);
jstring wcharToJString(wchar_t* wstr, JNIEnv *env);
jobject getNewArrayList(JNIEnv *env);
void arrayListAdd(JNIEnv *env, jobject list, jobject element);
jobject getNewMTPDevice(JNIEnv *env, PWSTR devId, PWSTR devFName, PWSTR devDesc, PWSTR devManu);