#include <string>
#include "jniHelpers.h"

using std::string;
using std::wstring;
using std::to_wstring;
using std::vector;

namespace LibJMTP {
    jstring wcharToJString(JNIEnv *env, const wchar_t *wstr) {
        size_t origSize = wcslen(wstr) + 1;
        size_t convertedChars = 0;
        const size_t newSize = origSize * 2;
        char* newString = new char[newSize];

        wcstombs_s(&convertedChars, newString, newSize, wstr, _TRUNCATE);
        jstring ret = env->NewStringUTF(newString);
        delete newString;

        return ret;
    }

    jobject ulonglongToJBigInt(JNIEnv * env, unsigned long long num)
    {
        jclass bigIntClass = env->FindClass(JBIGINT);

        string sig = "(";
        sig += JSTRING;
        sig += ")V";

        jmethodID bigIntConstructor = env->GetMethodID(bigIntClass, JCONSTRUCTOR, sig.c_str());

        // the number needs to be a string for BigInteger's constructor
        wstring strNum = to_wstring(num);
        jstring jStrNum = wcharToJString(env, strNum.c_str());

        return env->NewObject(bigIntClass, bigIntConstructor, jStrNum);
    }

    wchar_t* jStringToWchar(JNIEnv *env, jstring jStr)
    {
        if (jStr == NULL) {
            return nullptr;
        }
        
        const jchar *raw = env->GetStringChars(jStr, 0);
        jsize len = env->GetStringLength(jStr);
        wstring wStr;
        wchar_t *wStr_c = new wchar_t[len + 1];

        wStr.assign(raw, raw + len);
        wcscpy_s(wStr_c, len + 1, wStr.c_str());

        return wStr_c;
    }

    jobject getNewArrayList(JNIEnv *env) {
        jclass arrayListClass = env->FindClass(JARRLIST);
        jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
        return env->NewObject(arrayListClass, arrayListConstructor);
    }

    void arrayListAdd(JNIEnv *env, jobject list, jobject element) {
        jclass arrayListClass = env->FindClass(JARRLIST);
        jmethodID arrayListAdd = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
        env->CallBooleanMethod(list, arrayListAdd, element);
    }
	jobject toJMTPDeviceInfoList(JNIEnv * env, jobject obj, vector<MTPDeviceInfo> info)
	{
		jobject jlist = getNewArrayList(env);
		jclass deviceInfoClass = env->FindClass(JMTPDEVICEINFO);

		string sig = "(";
		sig += JMTPDEVICE;
		sig += JSTRING;
		sig += JSTRING;
		sig += JSTRING;
		sig += JSTRING;
		sig += ")V";

		jmethodID deviceInfoConstr = env->GetMethodID(deviceInfoClass, JCONSTRUCTOR, sig.c_str());
		
		for (auto iter = info.begin(); iter != info.end(); iter++) {
			jstring jDeviceId = wcharToJString(env, iter->deviceId.c_str());
			jstring jDescription = wcharToJString(env, iter->description.c_str());
			jstring jFriendlyName = wcharToJString(env, iter->friendlyName.c_str());
			jstring jManufacturer = wcharToJString(env, iter->manufacturer.c_str());

			jobject jInfo = env->NewObject(deviceInfoClass,
				deviceInfoConstr,
                obj,
				jDeviceId,
				jFriendlyName,
				jDescription,
				jManufacturer);

			arrayListAdd(env, jlist, jInfo);
		}
		return jlist;
	}
}