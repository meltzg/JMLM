#include <string>
#include <stack>
#include <map>
#include <vector>
#include "jniHelpers.h"

using std::string;
using std::wstring;
using std::to_wstring;
using std::stack;
using std::map;
using std::vector;

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
	const jchar *raw = env->GetStringChars(jStr, 0);
	jsize len = env->GetStringLength(jStr);
	wstring wStr;
	wchar_t *wStr_c = new wchar_t[len + 1];

	wStr.assign(raw, raw + len);
	wcscpy_s(wStr_c, len + 1, wStr.c_str());

	return wStr_c;
}

jobject mtpdToJMtpd(JNIEnv *env, MTPDevice mtpd)
{
	return getNewMTPDevice(env,
		mtpd.getId().c_str(),
		mtpd.getFriendlyName().c_str(),
		mtpd.getDescription().c_str(),
		mtpd.getManufacturer().c_str());
}

jobject jMtpotGetChildren(JNIEnv *env, jobject jmtpot) {
	jclass mtpObjectTreeClass = env->FindClass(JMTPOTREE);

	string sig = "()";
	sig += JLIST;

	jmethodID mtpObjectTreeGetChildren = env->GetMethodID(mtpObjectTreeClass, "getChildren", sig.c_str());
	return env->CallObjectMethod(jmtpot, mtpObjectTreeGetChildren);
}

jobject getJMTPotNode(JNIEnv *env, MTPObjectTree *mtpot) {
	jclass mtpObjectTreeClass = env->FindClass(JMTPOTREE);
	string sig = "(";
	sig += JSTRING;
	sig += JSTRING;
	sig += JSTRING;
	sig += JSTRING;
	sig += JSTRING;
	sig += JBIGINT;
	sig += JBIGINT;
	sig += JLIST;
	sig += ")V";
	jmethodID mtpObjectTreeConstructor = env->GetMethodID(mtpObjectTreeClass, JCONSTRUCTOR, sig.c_str());

	jstring jId = wcharToJString(env, mtpot->getId().c_str());
	jstring jParentId = wcharToJString(env, mtpot->getParentId().c_str());
	jstring jPersistId = wcharToJString(env, mtpot->getPersistId().c_str());
	jstring jName = wcharToJString(env, mtpot->getName().c_str());
	jstring jOrigName = wcharToJString(env, mtpot->getOrigName().c_str());

	jobject jSize = ulonglongToJBigInt(env, mtpot->getSize());
	jobject jCapacity = ulonglongToJBigInt(env, mtpot->getCapacity());

	jobject jChildren = getNewArrayList(env);

	/*for (auto child : mtpot->children) {
	arrayListAdd(env, jChildren, mtpotToJMtpot(env, child));
	}*/

	return env->NewObject(mtpObjectTreeClass, mtpObjectTreeConstructor, jId, jParentId, jPersistId, jName, jOrigName, jSize, jCapacity, jChildren);
}

jobject mtpotToJMtpot(JNIEnv * env, MTPObjectTree *mtpot)
{	
	stack<MTPObjectTree*> nodes;
	map<wstring, vector<wstring>> idToCIds;
	map<wstring, jobject> idToJNodes;
	nodes.push(mtpot);

	while (!nodes.empty()) {
		auto node = nodes.top();
		nodes.pop();

		idToJNodes[node->getId()] = getJMTPotNode(env, node);
		idToCIds[node->getId()];
		for (auto child : node->children) {
			nodes.push(child);
			idToCIds[node->getId()].push_back(child->getId());
		}
	}
	
	for (auto node : idToJNodes) {
		wstring id = node.first;
		jobject jNode = node.second;
		jobject childList = jMtpotGetChildren(env, jNode);
		const vector<wstring> children = idToCIds.at(id);
		for (auto cId : children) {
			arrayListAdd(env, childList, idToJNodes.at(cId));
		}
	}

	return idToJNodes.at(mtpot->getId());
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

jobject getNewMTPDevice(JNIEnv *env, const wchar_t *devId, const wchar_t *devFName, const wchar_t *devDesc, const wchar_t *devManu) {
	jclass mtpDeviceClass = env->FindClass(JMTPDEV);

	// (js, js, js)V
	string sig = "(";
	sig += JSTRING;
	sig += JSTRING;
	sig += JSTRING;
	sig += JSTRING;
	sig += ")V";

	jmethodID mtpDeviceConstructor = env->GetMethodID(mtpDeviceClass, JCONSTRUCTOR, sig.c_str());

	jstring jDevId = wcharToJString(env, devId);
	jstring jFName = wcharToJString(env, devFName);
	jstring jDDesc = wcharToJString(env, devDesc);
	jstring jDManu = wcharToJString(env, devManu);

	return env->NewObject(mtpDeviceClass, mtpDeviceConstructor, jDevId, jFName, jDDesc, jDManu);
}