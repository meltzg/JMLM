package org.meltzg.jmlm.device.models;

import java.math.BigInteger;
import java.util.ArrayList;

import org.meltzg.jmlm.content.models.AbstractContentTree;
import org.meltzg.jmlm.content.models.ContentRoot;
import org.meltzg.jmlm.content.models.FSAudioContentTree;

public class FSAudioContentDevice extends AbstractContentDevice {

	public FSAudioContentDevice(String deviceId, String friendlyName, String description, String manufacturer) {
		super(deviceId, friendlyName, description, manufacturer);
		
	}

	@Override
	public void buildContentRoot() {
		this.contentRoot = new ContentRoot(new FSAudioContentTree("", "ROOT", "", BigInteger.ZERO, BigInteger.ZERO, new ArrayList<AbstractContentTree>()));
	}

}
