package org.meltzg.jmlm.device.access;

import java.io.Closeable;
import java.util.List;

import org.meltzg.jmlm.device.models.ContentDevice;
import org.meltzg.jmlm.device.models.ContentTree;

/**
 * 
 */

/**
 * @author vader
 *
 */
public abstract class ContentInterface implements Closeable {

	protected static ContentInterface singleton;
	
	protected ContentInterface() {}
	
	public static ContentInterface getInstance()  {
		return singleton;
	}
	
	public abstract List<ContentDevice> getDevices();
	public abstract boolean selectDevice(String id);
	public abstract ContentTree getDeviceContent();
	public abstract String transferToDevice(String filepath, String destId, String destName);
	public abstract boolean removeFromDevice(String id, String stopId);
	public abstract boolean transferFromDevice(String id, String destFilepath);
}
