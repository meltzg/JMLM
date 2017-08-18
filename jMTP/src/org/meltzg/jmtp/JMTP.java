package org.meltzg.jmtp;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.meltzg.jmtp.models.MTPDevice;
import org.meltzg.jmtp.models.MTPObjectTree;

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
	
	public native List<MTPDevice> getDevices();
	public native boolean selectDevice(String id);
	public native MTPObjectTree getDeviceContent();
	public native String transferToDevice(String filepath, String destId, String destName);
	public native boolean removeFromDevice(String id, String stopId);
	public native boolean transferFromDevice(String id, String destFilepath);
	
	private native long initCOM();
	private native void closeCOM();
	
	static {
		System.loadLibrary("libJMTP");
	}
}
