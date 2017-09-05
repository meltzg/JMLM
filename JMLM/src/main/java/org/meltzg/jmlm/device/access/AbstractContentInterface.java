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
public abstract class AbstractContentInterface implements Closeable, IContentInterface {

	protected static IContentInterface singleton;
	
	protected AbstractContentInterface() {}
	
	public static IContentInterface getInstance()  {
		return singleton;
	}
	
	public abstract List<AbstractContentDevice> getDevices();
	public abstract boolean selectDevice(String id);
	/* (non-Javadoc)
	 * @see org.meltzg.jmlm.device.access.IContentInterface#getDeviceContent()
	 */
	@Override
	public abstract AbstractContentTree getDeviceContent();
	/* (non-Javadoc)
	 * @see org.meltzg.jmlm.device.access.IContentInterface#getDeviceContent(java.lang.String)
	 */
	@Override
	public abstract AbstractContentTree getDeviceContent(String rootId);
	/* (non-Javadoc)
	 * @see org.meltzg.jmlm.device.access.IContentInterface#transferToDevice(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public abstract AbstractContentTree transferToDevice(String filepath, String destId, String destName);
	/* (non-Javadoc)
	 * @see org.meltzg.jmlm.device.access.IContentInterface#removeFromDevice(java.lang.String, java.lang.String)
	 */
	@Override
	public abstract String removeFromDevice(String id, String stopId);
	/* (non-Javadoc)
	 * @see org.meltzg.jmlm.device.access.IContentInterface#transferFromDevice(java.lang.String, java.lang.String)
	 */
	@Override
	public abstract boolean transferFromDevice(String id, String destFilepath);
}
