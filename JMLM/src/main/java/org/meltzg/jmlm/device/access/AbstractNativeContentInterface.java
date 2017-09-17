package org.meltzg.jmlm.device.access;

import java.io.Closeable;
import java.util.List;

import org.meltzg.jmlm.device.models.AbstractContentDevice;

/**
 * 
 */

/**
 * @author vader
 *
 */
public abstract class AbstractNativeContentInterface implements Closeable, IContentInterface {

	protected static IContentInterface singleton;
	
	protected AbstractNativeContentInterface() {}
	
	public static IContentInterface getInstance()  {
		return singleton;
	}
	
	public abstract List<AbstractContentDevice> getDevices();
	public abstract boolean selectDevice(String id);
}