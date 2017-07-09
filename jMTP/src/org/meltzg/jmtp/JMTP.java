package org.meltzg.jmtp;

public class JMTP {
	public native void sayHello();
	
	static {
		System.loadLibrary("CLibJMTP");
	}
	
	public static void main(String[] args) {
		new JMTP().sayHello();
	}
}
