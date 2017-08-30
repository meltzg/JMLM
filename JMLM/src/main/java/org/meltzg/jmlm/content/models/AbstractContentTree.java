package org.meltzg.jmlm.content.models;

import java.math.BigInteger;
import java.util.List;

public abstract class AbstractContentTree {

	protected String id;
	protected String parentId;
	protected String origName;
	protected BigInteger size;
	protected BigInteger capacity;
	protected List<AbstractContentTree> children;
	
	public AbstractContentTree() {
	}
	
	public AbstractContentTree(AbstractContentTree other) {
		init(other.id, other.parentId, other.origName, other.size, other.capacity, other.children);
	}
	
	protected void init(String id, String parentId, String origName, BigInteger size,
			BigInteger capacity, List<AbstractContentTree> children) {
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

	public List<AbstractContentTree> getChildren() {
		return children;
	}

	public void setChildren(List<AbstractContentTree> children) {
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

		for (AbstractContentTree oTree : children) {
			str += oTree.toPrettyString(level + 1);
		}

		return str;		
	}
}
