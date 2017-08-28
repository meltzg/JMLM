package org.meltzg.jmtp;

import java.io.IOException;
import java.util.List;

import org.meltzg.jmlm.device.access.ContentInterface;
import org.meltzg.jmlm.device.models.ContentDevice;
import org.meltzg.jmtp.models.MTPDevice;
import org.meltzg.jmtp.models.MTPObjectTree;

/**
 * A singleton implementation for accessing and modifying the content of MTP devices
 * 
 * @author Greg Meltzer
 *
 */
public class JMTP extends ContentInterface {
	
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
	public native List<ContentDevice> getDevices();
	/**
	 * Selects a device by ID to operate on.  All subsequent content access method calls will use the selected device
	 * 
	 * @param id The ID of the device to select
	 * @return boolean if selection is successful;
	 */
	public native boolean selectDevice(String id);
	/**
	 * Enumerates the selected device, returning an {@link MTPObjectTree}.  If unsuccessful, returns null.
	 * 
	 * @return {@link MTPObjectTree} content of the selected device.
	 */
	public native MTPObjectTree getDeviceContent();
	/**
	 * Transfers a file from the computer to the selected device
	 * 
	 * @param filepath The path of the file to transfer
	 * @param destId The ID of the location on the device to transfer the file under
	 * @param destName The name to give the transfered file.  The '/' can be used to create intermediate folders (ex. "these/are/folders/for/file.mp3"
	 * @return The ID of the file that was transfered.  null if unsuccessful
	 */
	public native String transferToDevice(String filepath, String destId, String destName);
	/**
	 * Removes an object from a device.  If the specified object has children, all of its children will also be removed.
	 * If stopId is specified, all parent objects with no more children will be removed until stopId is reached or
	 * an object with children is encountered.
	 * 
	 * @param id ID of the object to remove
	 * @param stopId ID of the object to stop deleting parents
	 * @return true if successful, false otherwise
	 */
	public native boolean removeFromDevice(String id, String stopId);
	/**
	 * Transfers a file from the object to the computer
	 * 
	 * @param id ID of the object to transfer
	 * @param destFilepath path and name to transfer the file to.  all intermediate folders will be created if they do not exist.
	 * @return
	 */
	public native boolean transferFromDevice(String id, String destFilepath);
	
	/**
	 * Initializes any necessary interfaces before a connection is possible
	 * 
	 * @return error code
	 */
	private native long initCOM();
	/**
	 * Cleanup the connection to the device
	 */
	private native void closeCOM();
	
	static {
		System.loadLibrary("libJMTP");
		singleton = new JMTP();
	}
}
