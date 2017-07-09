#include <jni.h>
#include <iostream>
#include "../../jMTP/bin/org_meltzg_jmtp_JMTP.h"

using std::cout;
using std::endl;

JNIEXPORT void JNICALL Java_org_meltzg_jmtp_JMTP_sayHello
(JNIEnv *env, jobject obj) {
	cout << "Hello World" << endl;
}