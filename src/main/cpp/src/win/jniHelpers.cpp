#include <string>
#include <sstream>
#include "jniHelpers.h"

using std::string;
using std::wstring;
using std::ostringstream;
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

		ostringstream sig;
		sig << "("
			<< JSTRING
			<< ")V";

        jmethodID bigIntConstructor = env->GetMethodID(bigIntClass, JCONSTRUCTOR, sig.str().c_str());

        // the number needs to be a string for BigInteger's constructor
        wstring strNum = to_wstring(num);
        jstring jStrNum = wcharToJString(env, strNum.c_str());

        return env->NewObject(bigIntClass, bigIntConstructor, jStrNum);
    }

    wstring jStringToWString(JNIEnv *env, jstring jStr)
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

		wstring ret(wStr_c);
		delete[] wStr_c;

        return ret;
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
	jobject toJMTPDeviceInfo(JNIEnv * env, jobject obj, MTPDeviceInfo info)
	{
		jclass deviceInfoClass = env->FindClass(JMTPDEVICEINFO);

		ostringstream sig;
		sig << "("
			<< JMTPDEVICE
			<< JSTRING
			<< JSTRING
			<< JSTRING
			<< JSTRING
			<< ")V";

		jmethodID deviceInfoConstr = env->GetMethodID(deviceInfoClass, JCONSTRUCTOR, sig.str().c_str());

		jstring jDeviceId = wcharToJString(env, info.deviceId.c_str());
		jstring jDescription = wcharToJString(env, info.description.c_str());
		jstring jFriendlyName = wcharToJString(env, info.friendlyName.c_str());
		jstring jManufacturer = wcharToJString(env, info.manufacturer.c_str());

		jobject jInfo = env->NewObject(deviceInfoClass,
			deviceInfoConstr,
			obj,
			jDeviceId,
			jFriendlyName,
			jDescription,
			jManufacturer);

		return jInfo;
	}
	jobject toJMTPDeviceInfoList(JNIEnv * env, jobject obj, vector<MTPDeviceInfo> info)
	{
		jobject jlist = getNewArrayList(env);
		
		
		for (auto iter = info.begin(); iter != info.end(); iter++) {
			jobject jInfo = toJMTPDeviceInfo(env, obj, *iter);

			arrayListAdd(env, jlist, jInfo);
		}
		return jlist;
	}
	
	jobject toJMTPContentNode(JNIEnv * env, MTPContentNode node)
	{
		jclass contentNodeClass = env->FindClass(JMTPCONTENTNODE);

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

		jmethodID contentNodeConstr = env->GetMethodID(contentNodeClass, JCONSTRUCTOR, sig.str().c_str());

		jstring id = wcharToJString(env, node.id.c_str());
		jstring pId = wcharToJString(env, node.pId.c_str());
		jstring name = wcharToJString(env, node.name.c_str());
		jstring origName = wcharToJString(env, node.origName.c_str());
		jobject size = ulonglongToJBigInt(env, node.size);
		jobject capacity = ulonglongToJBigInt(env, node.capacity);

		jobject jNode = env->NewObject(contentNodeClass,
			contentNodeConstr,
			id,
			pId,
			name,
			origName,
			node.isDir,
			size,
			capacity);

		return jNode;
	}
}