package org.meltzg.jmtp;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.meltzg.jmtp.mtp.MTPDevice;

public class JMTP implements Closeable{
	private boolean closed = false;
	
	public JMTP() {
		initCOM();
	}
	
	@Override
	public void close() throws IOException {
		closeCOM();
		closed = true;
	}
	
	@Override
	protected void finalize() {
		if (!closed) {
			System.err.println("WARNING:  " + getClass().getCanonicalName() + ".close() was not called");
			try {
				close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public native void sayHello();
	public native String getHello();
	public native List<MTPDevice> getDevices();
	
	private native long initCOM();
	private native void closeCOM();
	
	static {
		System.loadLibrary("CLibJMTP");
	}
}
