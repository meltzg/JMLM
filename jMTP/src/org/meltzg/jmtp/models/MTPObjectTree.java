package org.meltzg.jmtp.models;

import java.math.BigInteger;
import java.util.List;

public class MTPObjectTree {
	private String id;
	private String parentId;
	private String persistId;
	private String name;
	private String origName;
	private BigInteger size;
	private BigInteger capacity;
	private List<MTPObjectTree> children;
	
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
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getPersistId() {
		return persistId;
	}
	public void setPersistId(String persistId) {
		this.persistId = persistId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOrigName() {
		return origName;
	}
	public void setOrigName(String origName) {
		this.origName = origName;
	}
	public BigInteger getSize() {
		return size;
	}
	public void setSize(BigInteger size) {
		this.size = size;
	}
	public BigInteger getCapacity() {
		return capacity;
	}
	public void setCapacity(BigInteger capacity) {
		this.capacity = capacity;
	}
	public List<MTPObjectTree> getChildren() {
		return children;
	}
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
