package org.meltzg.jmtp.models;

import java.math.BigInteger;
import java.util.List;

import org.meltzg.jmlm.device.models.ContentTree;

/**
 * Represents the contents of an MTP device.  Each node is an object on the MTP device
 * 
 * @author Greg Meltzer
 *
 */
public class MTPObjectTree extends ContentTree {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1366586814273218967L;
	
	private String persistId;
	private String name;
	
	/**
	 * @param id
	 * @param parentId
	 * @param persistId
	 * @param name
	 * @param origName
	 * @param size
	 * @param capacity
	 * @param children
	 */
	public MTPObjectTree(String id, String parentId, String persistId, String name, String origName, BigInteger size,
			BigInteger capacity, List<ContentTree> children) {
		init(id, parentId, persistId, name, origName, size, capacity, children);
	}
	
	public MTPObjectTree(MTPObjectTree other) {
		init(other.id, other.parentId, other.persistId, other.name, other.origName, other.size, other.capacity, other.children);
	}
	
	protected void init(String id, String parentId, String persistId, String name, String origName, BigInteger size,
			BigInteger capacity, List<ContentTree> children) {
		super.init(id, parentId, origName, size, capacity, children);
		this.persistId = persistId;
		this.name = name;
	}

	/**
	 * @return
	 */
	public String getPersistId() {
		return persistId;
	}
	/**
	 * @param persistId
	 */
	public void setPersistId(String persistId) {
		this.persistId = persistId;
	}
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "MTPObjectTree [id=" + id + ", parentId=" + parentId + ", persistId=" + persistId + ", name=" + name
				+ ", origName=" + origName + ", size=" + size + ", capacity=" + capacity + ", children=" + children
				+ "]";
	}
}
