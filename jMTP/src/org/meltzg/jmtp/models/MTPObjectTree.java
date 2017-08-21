package org.meltzg.jmtp.models;

import java.math.BigInteger;
import java.util.List;

/**
 * Represents the contents of an MTP device.  Each node is an object on the MTP device
 * 
 * @author Greg Meltzer
 *
 */
public class MTPObjectTree {
	private String id;
	private String parentId;
	private String persistId;
	private String name;
	private String origName;
	private BigInteger size;
	private BigInteger capacity;
	private List<MTPObjectTree> children;
	
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
			BigInteger capacity, List<MTPObjectTree> children) {
		this.id = id;
		this.parentId = parentId;
		this.persistId = persistId;
		this.name = name;
		this.origName = origName;
		this.size = size;
		this.capacity = capacity;
		this.children = children;
	}
	
	/**
	 * @return
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return
	 */
	public String getParentId() {
		return parentId;
	}
	/**
	 * @param parentId
	 */
	public void setParentId(String parentId) {
		this.parentId = parentId;
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
	/**
	 * @return
	 */
	public String getOrigName() {
		return origName;
	}
	/**
	 * @param origName
	 */
	public void setOrigName(String origName) {
		this.origName = origName;
	}
	/**
	 * @return
	 */
	public BigInteger getSize() {
		return size;
	}
	/**
	 * @param size
	 */
	public void setSize(BigInteger size) {
		this.size = size;
	}
	/**
	 * @return
	 */
	public BigInteger getCapacity() {
		return capacity;
	}
	/**
	 * @param capacity
	 */
	public void setCapacity(BigInteger capacity) {
		this.capacity = capacity;
	}
	/**
	 * @return
	 */
	public List<MTPObjectTree> getChildren() {
		return children;
	}
	/**
	 * @param children
	 */
	public void setChildren(List<MTPObjectTree> children) {
		this.children = children;
	}
	
	private String toPrettyString(int level) {
		String str = "";
		for (int i = 0; i < level; i++) {
			str += "\t";
		}
		
		str += "MTPObjectTree [id=" + id + ", parentId=" + parentId + ", persistId=" + persistId + ", name=" + name
				+ ", origName=" + origName + ", size=" + size + ", capacity=" + capacity + "]\n";
		
		for (MTPObjectTree oTree : children) {
			str += oTree.toPrettyString(level + 1);
		}
		
		return str;
	}
	
	/**
	 * Returns a formatted version of toString that can be used to display the contents as a tab delimited tree
	 * @return
	 */
	public String toPrettyString() {
		return toPrettyString(0);
	}

	@Override
	public String toString() {
		return "MTPObjectTree [id=" + id + ", parentId=" + parentId + ", persistId=" + persistId + ", name=" + name
				+ ", origName=" + origName + ", size=" + size + ", capacity=" + capacity + ", children=" + children
				+ "]";
	}
}
