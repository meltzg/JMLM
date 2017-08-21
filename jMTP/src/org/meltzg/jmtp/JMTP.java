package org.meltzg.jmtp;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.meltzg.jmtp.models.MTPDevice;
import org.meltzg.jmtp.models.MTPObjectTree;

/**
 * A singleton implementation for accessing MTP devices
 * 
 * @author vader
 *
 */
public class JMTP implements Closeable{
	
	private static JMTP singleton;
	
	private boolean closed = false;
	
	private JMTP() {
		initCOM();
	}
	
	/**
	 * JMTP instance retrieval function.
	 * <p>
	 * Since the underlying native implementation maintains certain information at runtime, only a single instance of JMTP can be instantiated
	 * @return The JMTP singleton
	 */
	public static JMTP getInstance() {
		return singleton;
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
	
	/**
	 * @return a List of {@link MTPDevice}s currently attached to the machine
	 */
	public native List<MTPDevice> getDevices();
	/**
	 * Selects a device by ID to operate on.  All subsequent content access method calls will use the selected device
	 * @param id The ID of the device to select
	 * @return boolean if selection is successful;
	 */
	public native boolean selectDevice(String id);
	/**
	 * Enumerates the selected device, returning an {@link MTPObjectTree}
	 * @return
	 */
	public native MTPObjectTree getDeviceContent();
	/**
	 * @param filepath
	 * @param destId
	 * @param destName
	 * @return
	 */
	public native String transferToDevice(String filepath, String destId, String destName);
	/**
	 * @param id
	 * @param stopId
	 * @return
	 */
	public native boolean removeFromDevice(String id, String stopId);
	/**
	 * @param id
	 * @param destFilepath
	 * @return
	 */
	public native boolean transferFromDevice(String id, String destFilepath);
	
	/**
	 * @return
	 */
	private native long initCOM();
	/**
	 * 
	 */
	private native void closeCOM();
	
	static {
		System.loadLibrary("libJMTP");
		singleton = new JMTP();
	}
}
