package org.meltzg.jmlm.device.access;

import org.meltzg.jmlm.content.models.AbstractContentTree;

public interface IContentInterface {

	AbstractContentTree getDeviceContent();

	AbstractContentTree getDeviceContent(String rootId);

	AbstractContentTree transferToDevice(String filepath, String destId, String destName);

	String removeFromDevice(String id, String stopId);

	boolean transferFromDevice(String id, String destFilepath);

}