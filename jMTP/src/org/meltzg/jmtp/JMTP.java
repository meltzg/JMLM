package org.meltzg.jmtp;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.meltzg.jmtp.mtp.MTPDevice;

public class JMTP implements Closeable{
	private boolean closed = false;
	
	public JMTP() {
		initCOM();
		int i = 0;
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
	
	public native List<MTPDevice> getDevices();
	
	private native long initCOM();
	private native void closeCOM();
	
	static {
		System.loadLibrary("libJMTP");
	}
}
