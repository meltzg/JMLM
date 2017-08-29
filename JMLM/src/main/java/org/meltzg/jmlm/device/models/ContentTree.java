package org.meltzg.jmlm.device.models;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

public abstract class ContentTree implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7574716142670335129L;

	protected String id;
	protected String parentId;
	protected String origName;
	protected BigInteger size;
	protected BigInteger capacity;
	protected List<ContentTree> children;
	
	public ContentTree() {
		
	}
	
	public ContentTree(ContentTree other) {
		init(other.id, other.parentId, other.origName, other.size, other.capacity, other.children);
	}
	
	protected void init(String id, String parentId, String origName, BigInteger size,
			BigInteger capacity, List<ContentTree> children) {
		this.id = id;
		this.parentId = parentId;
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

	public List<ContentTree> getChildren() {
		return children;
	}

	public void setChildren(List<ContentTree> children) {
		this.children = children;
	}

	/**
	 * Returns a formatted version of toString that can be used to display the
	 * contents as a tab delimited tree
	 * 
	 * @return
	 */
	public String toPrettyString() {
		return toPrettyString(0);
	}

	protected String toPrettyString(int level) {
		String str = "";
		for (int i = 0; i < level; i++) {
			str += "\t";
		}

		str += "MTPObjectTree [id=" + id + ", parentId=" + parentId + ", origName=" + origName + ", size=" + size
				+ ", capacity=" + capacity + "]\n";

		for (ContentTree oTree : children) {
			str += oTree.toPrettyString(level + 1);
		}

		return str;		
	}
}
