package org.meltzg.jmlm.device.access;

import java.io.Closeable;
import java.util.List;

import org.meltzg.jmlm.content.models.AbstractContentTree;
import org.meltzg.jmlm.device.models.AbstractContentDevice;

/**
 * 
 */

/**
 * @author vader
 *
 */
public abstract class AbstractContentInterface implements Closeable {

	protected static AbstractContentInterface singleton;
	
	protected AbstractContentInterface() {}
	
	public static AbstractContentInterface getInstance()  {
		return singleton;
	}
	
	public abstract List<AbstractContentDevice> getDevices();
	public abstract boolean selectDevice(String id);
	public abstract AbstractContentTree getDeviceContent();
	public abstract AbstractContentTree getDeviceContent(String rootId);
	public abstract String transferToDevice(String filepath, String destId, String destName);
	public abstract boolean removeFromDevice(String id, String stopId);
	public abstract boolean transferFromDevice(String id, String destFilepath);
}
